package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.guis.CircesBrandGUI;
import com.atlasplugins.atlasenchants.guis.Gui;
import com.atlasplugins.atlasenchants.guis.GuiManager;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCircesBrand;
import com.atlasplugins.atlasenchants.utils.RenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Circe's Brand - renaming a held item by typing the new name in chat.
 *
 * The original design used a custom anvil GUI so the player got a real text field. That is not
 * achievable through the Bukkit API: an inventory created with InventoryType.ANVIL never fires
 * PrepareAnvilEvent (PaperMC/Paper#9892, closed as not planned), so nothing can populate the
 * result slot or zero the repair cost. Doing it properly needs per-version NMS, which this
 * plugin otherwise avoids. Hence the chat flow below.
 *
 * Right-click consumes the Brand and opens a short window in which the player's next chat
 * message is intercepted and applied as the name of whatever they are holding. The Brand is
 * refunded if that window closes for any reason without a successful rename.
 */
public class CircesBrandEvent implements Listener {

    private final Main main;

    /** Players currently mid-rename, keyed by UUID. */
    private final Map<UUID, PendingRename> pending = new HashMap<>();

    public CircesBrandEvent(Main main) {
        this.main = main;
    }

    /** A consumed Brand awaiting a chat message, held so it can be given back on failure. */
    private static final class PendingRename {
        private final ItemStack brand;
        private BukkitTask timeout;

        private PendingRename(ItemStack brand) {
            this.brand = brand;
        }
    }

    // ------------------------------------------------------------------------------------
    // Starting a rename
    // ------------------------------------------------------------------------------------

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // PlayerInteractEvent fires once per hand; without this the Brand is consumed twice.
        if (e.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = e.getItem();
        if (!CreateCircesBrand.isCircesBrand(item)) return;

        e.setCancelled(true);

        Player player = e.getPlayer();

        if (!main.getEnchantmentsConfig().getBoolean("CircesBrand.CircesBrand-Enabled", true)) {
            sendMessages(player, "CircesBrandMessages.DisabledBrand");
            return;
        }

        if (!player.hasPermission("atlasenchants.circesbrand.use")) {
            for (String msg : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-NoPermissions")) {
                player.sendMessage(Main.color(main.setPlaceholders(player, msg)));
            }
            return;
        }

        // ANVIL mode: open the real anvil menu and let CircesBrandGUI take it from here. The
        // Brand is not consumed until the player takes the renamed item out.
        if (!isChatMode()) {
            new CircesBrandGUI(main, player).open();
            playSound(player, "CircesBrandSounds.OpenBrand", "BLOCK_ANVIL_PLACE");
            return;
        }

        // Already waiting on a name - do not consume a second Brand.
        if (pending.containsKey(player.getUniqueId())) {
            sendMessages(player, "CircesBrandMessages.AlreadyBranding");
            return;
        }

        // Consume one Brand up front, keeping a copy so it can be handed back if the player
        // never completes the rename.
        ItemStack consumed = item.clone();
        consumed.setAmount(1);
        item.setAmount(item.getAmount() - 1);

        PendingRename entry = new PendingRename(consumed);
        pending.put(player.getUniqueId(), entry);

        int timeoutSeconds = Math.max(5, main.getEnchantmentsConfig().getInt("CircesBrand.CircesBrand-Chat-Timeout-Seconds", 60));
        entry.timeout = Bukkit.getScheduler().runTaskLater(main, () -> {
            if (pending.remove(player.getUniqueId()) == null) return;
            giveBack(player, consumed);
            sendMessages(player, "CircesBrandMessages.TimedOut");
        }, timeoutSeconds * 20L);

        sendMessages(player, "CircesBrandMessages.Prompt");
        playSound(player, "CircesBrandSounds.OpenBrand", "BLOCK_ANVIL_PLACE");
    }

    /**
     * The Brand is a NAME_TAG by default, so without this it would still work as a vanilla name
     * tag on the first mob the player right-clicks - consuming it for nothing.
     */
    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (CreateCircesBrand.isCircesBrand(e.getPlayer().getInventory().getItemInMainHand())) {
            e.setCancelled(true);
        }
    }

    // ------------------------------------------------------------------------------------
    // Applying the name
    // ------------------------------------------------------------------------------------

    /**
     * Intercept the next chat message from a player who is mid-rename. Runs off the main thread,
     * so the message is only inspected here - everything touching the inventory is handed to a
     * sync task.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        if (!pending.containsKey(player.getUniqueId())) return;

        // Swallow the message either way - the player was typing a name, not talking.
        e.setCancelled(true);

        String typed = e.getMessage();
        Bukkit.getScheduler().runTask(main, () -> applyRename(player, typed));
    }

    private void applyRename(Player player, String typed) {
        PendingRename entry = pending.remove(player.getUniqueId());
        if (entry == null) return; // timed out between the chat message and this task
        if (entry.timeout != null) entry.timeout.cancel();

        String cancelWord = main.getEnchantmentsConfig().getString("CircesBrand.CircesBrand-Cancel-Word", "cancel");
        if (typed.trim().equalsIgnoreCase(cancelWord)) {
            giveBack(player, entry.brand);
            sendMessages(player, "CircesBrandMessages.CancelledRename");
            return;
        }

        ItemStack target = player.getInventory().getItemInMainHand();
        if (!isRenameable(target)) {
            giveBack(player, entry.brand);
            sendMessages(player, "CircesBrandMessages.InvalidItem");
            return;
        }

        String visible = RenameUtils.stripAll(typed);
        int maxLength = main.getEnchantmentsConfig().getInt("CircesBrand.CircesBrand-Max-Name-Length", 32);

        if (visible.trim().isEmpty() || visible.length() > maxLength) {
            giveBack(player, entry.brand);
            sendMessages(player, "CircesBrandMessages.InvalidName");
            return;
        }

        if (isBlockedName(visible)) {
            giveBack(player, entry.brand);
            sendMessages(player, "CircesBrandMessages.BlockedName");
            return;
        }

        boolean allowFormatting = player.hasPermission("atlasenchants.circesbrand.format");

        // Mutate the existing meta rather than building a fresh one - the item's custom enchant
        // PDC data and lore live on that meta and would be destroyed by a rebuild.
        ItemMeta meta = target.getItemMeta();
        meta.setDisplayName(RenameUtils.translate(typed, allowFormatting));
        target.setItemMeta(meta);

        sendMessages(player, "CircesBrandMessages.RenameItem");
        playSound(player, "CircesBrandSounds.RenameItem", "BLOCK_ANVIL_USE");
    }

    /**
     * Build the anvil's result slot. Vanilla will not produce a result for a custom rename, so
     * without this the preview stays empty and the player can never take their item out.
     */
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        if (!(e.getView().getPlayer() instanceof Player player)) return;

        Gui gui = GuiManager.getOpenGui(player);
        if (!(gui instanceof CircesBrandGUI brandGui)) return;
        if (!gui.getInventory().equals(e.getInventory())) return;

        brandGui.updateResult(e);
    }

    /**
     * Drags are not routed by GuiListener, so the Brand menu gets them here - without this a drag
     * could scatter part of a Brand stack across the anvil's input slots.
     */
    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        Gui gui = GuiManager.getOpenGui(player);
        if (!(gui instanceof CircesBrandGUI brandGui)) return;
        if (!gui.getInventory().equals(e.getView().getTopInventory())) return;

        brandGui.handleDrag(e);
    }

    // ------------------------------------------------------------------------------------
    // Cleanup
    // ------------------------------------------------------------------------------------

    /** Refund on quit, so a disconnect mid-rename never silently eats the Brand. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        PendingRename entry = pending.remove(e.getPlayer().getUniqueId());
        if (entry == null) return;
        if (entry.timeout != null) entry.timeout.cancel();

        // The player is leaving, so this cannot go into their live inventory - drop it where
        // they stood rather than destroying it.
        e.getPlayer().getWorld().dropItemNaturally(e.getPlayer().getLocation(), entry.brand);
    }

    // ------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------

    /**
     * CHAT mode is the fallback for servers where the anvil menu misbehaves. ANVIL is the
     * default and gives the player a real text field instead of typing into chat.
     */
    private boolean isChatMode() {
        return "CHAT".equalsIgnoreCase(
                main.getEnchantmentsConfig().getString("CircesBrand.CircesBrand-Mode", "ANVIL"));
    }

    private void giveBack(Player player, ItemStack item) {
        if (item == null) return;
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack spill : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), spill);
        }
    }

    /**
     * Whether this stack may be renamed. Stacks of more than one are refused outright - renaming
     * a stack would either rename all of it or silently split it, and neither is what the player
     * asked for.
     */
    private boolean isRenameable(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) return false;
        if (item.getAmount() > 1) return false;
        if (CreateCircesBrand.isCircesBrand(item)) return false;

        List<String> allowed = main.getEnchantmentsConfig().getStringList("CircesBrand.CircesBrand-Applicable-Items");
        if (allowed.isEmpty()) return true; // empty list means "anything"

        String type = item.getType().name();
        for (String entry : allowed) {
            if (entry.equalsIgnoreCase(type)) return true;
            // Suffix form, e.g. "_PICKAXE" matches every pickaxe material.
            if (entry.startsWith("_") && type.endsWith(entry.toUpperCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private boolean isBlockedName(String visibleName) {
        List<String> blocked = main.getEnchantmentsConfig().getStringList("CircesBrand.CircesBrand-Blocked-Words");
        if (blocked.isEmpty()) return false;

        String lower = visibleName.toLowerCase(Locale.ROOT);
        for (String word : blocked) {
            if (!word.isEmpty() && lower.contains(word.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private void playSound(Player player, String path, String fallback) {
        if (!main.getSettingsConfig().getBoolean(path + ".Toggle", false)) return;
        Sound sound = Main.getSound(main.getSettingsConfig().getString(path + ".Sound", fallback));
        if (sound == null) return;
        float volume = (float) main.getSettingsConfig().getDouble(path + ".Volume", 1);
        float pitch = (float) main.getSettingsConfig().getDouble(path + ".Pitch", 1);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private void sendMessages(Player player, String path) {
        if (!main.getSettingsConfig().getBoolean(path + ".Toggle", false)) return;
        for (String msg : main.getSettingsConfig().getStringList(path + ".Message")) {
            player.sendMessage(Main.color(main.setPlaceholders(player, msg)));
        }
    }
}
