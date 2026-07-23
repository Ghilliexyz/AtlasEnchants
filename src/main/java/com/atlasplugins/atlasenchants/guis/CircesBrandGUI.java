package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCircesBrand;
import com.atlasplugins.atlasenchants.utils.RenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * The Circe's Brand renaming GUI - a real anvil, so the player gets the vanilla text field.
 *
 * The view is built with MenuType.ANVIL, NOT Bukkit.createInventory(InventoryType.ANVIL). The
 * latter produces a non-functional anvil for which PrepareAnvilEvent never fires (see
 * PaperMC/Paper#9892, closed as not planned), which leaves the result slot permanently empty and
 * the repair cost untouchable. MenuType creates a genuine anvil menu and the event fires
 * normally.
 *
 * Slot 0 holds the item being renamed, slot 1 holds the Brand, slot 2 holds the preview. The
 * Brand is consumed only when the player actually takes the result out; closing the GUI at any
 * other point returns both inputs untouched.
 */
public class CircesBrandGUI extends Gui {

    private static final int SLOT_TOOL = 0;
    private static final int SLOT_BRAND = 1;
    private static final int SLOT_RESULT = 2;

    private final Main main;
    private final AnvilView view;

    // Set once the rename is taken. Stops onInventoryClose from also refunding the inputs, which
    // would otherwise hand back the Brand we just consumed.
    private boolean committed = false;

    public CircesBrandGUI(Main main, Player player) {
        this(main, player, MenuType.ANVIL.create(player,
                Main.color(main.getMenusConfig().getString("CircesBrand-Gui.Title", "&5&lBranding"))));
    }

    private CircesBrandGUI(Main main, Player player, AnvilView view) {
        super(player, view.getTopInventory());
        this.main = main;
        this.view = view;
    }

    @Override
    public void setupItems() {
        // Nothing to place - an anvil has no decorative slots, both inputs are filled by the player.
    }

    /** Opens the anvil view rather than the bare Inventory, which would not carry the menu. */
    @Override
    public void open() {
        setupItems();
        GuiManager.setOpenGui(player, this);
        player.openInventory(view);
    }

    // ------------------------------------------------------------------------------------
    // Result preview
    // ------------------------------------------------------------------------------------

    /**
     * Rebuild the result slot. Called from PrepareAnvilEvent whenever an input changes or the
     * player types, since vanilla will not produce a result for a custom rename on its own.
     */
    public void updateResult(PrepareAnvilEvent event) {
        ItemStack tool = inventory.getItem(SLOT_TOOL);
        ItemStack brand = inventory.getItem(SLOT_BRAND);

        if (tool == null || brand == null || !CreateCircesBrand.isCircesBrand(brand) || !isRenameable(tool)) {
            event.setResult(null);
            return;
        }

        String typed = event.getView().getRenameText();
        if (typed == null || typed.trim().isEmpty()) {
            event.setResult(null);
            return;
        }

        String visible = RenameUtils.stripAll(typed);
        int maxLength = main.getEnchantmentsConfig().getInt("CircesBrand.CircesBrand-Max-Name-Length", 32);
        if (visible.trim().isEmpty() || visible.length() > maxLength) {
            event.setResult(null);
            return;
        }

        if (isBlockedName(visible)) {
            event.setResult(null);
            return;
        }

        boolean allowFormatting = player.hasPermission("atlasenchants.circesbrand.format");

        // Mutate the existing meta rather than building a fresh one - the tool's custom enchant
        // PDC data and lore live on that meta and would be destroyed by a rebuild.
        ItemStack result = tool.clone();
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(RenameUtils.translate(typed, allowFormatting));
        result.setItemMeta(meta);

        event.setResult(result);

        // A custom anvil result is only accepted when a repair cost of >= 0 is set, so this is
        // required for the preview to appear at all, not merely to make it free.
        zeroRepairCost(event.getView());

        Bukkit.getScheduler().runTask(main, () -> {
            if (!(player.getOpenInventory() instanceof AnvilView open)) return;
            if (!open.getTopInventory().equals(inventory)) return;

            // The server recalculates the cost after this event returns, so it is zeroed again.
            zeroRepairCost(open);

            // The anvil screen names the result slot client-side from the text field as the
            // player types, so what they see on hover is the client's own plain-text guess and
            // not the coloured item built above. Re-sending the server's item replaces it.
            inventory.setItem(SLOT_RESULT, result);
            player.updateInventory();
        });
    }

    private void zeroRepairCost(AnvilView anvilView) {
        anvilView.setRepairCost(0);
        anvilView.setRepairItemCountCost(0);
        // Must be ABOVE the cost, not equal to it: vanilla treats "cost >= maximum" as
        // "Too Expensive!", so a maximum of 0 blocks the free rename it is meant to allow.
        anvilView.setMaximumRepairCost(Integer.MAX_VALUE);
    }

    // ------------------------------------------------------------------------------------
    // Clicks
    // ------------------------------------------------------------------------------------

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Hotbar swaps, offhand swaps and double-click-collect can all move items in or out of
        // slots we are mid-way through validating, so they are refused outright.
        ClickType click = event.getClick();
        if (click == ClickType.NUMBER_KEY
                || click == ClickType.SWAP_OFFHAND
                || click == ClickType.DOUBLE_CLICK
                || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }

        int raw = event.getRawSlot();
        boolean inTop = raw >= 0 && raw < inventory.getSize();

        if (!inTop) {
            if (click.isShiftClick()) {
                event.setCancelled(true);
                routeShiftClick(event);
            }
            return; // ordinary clicks in the player's own inventory are their business
        }

        ItemStack cursor = event.getCursor();

        switch (raw) {
            case SLOT_RESULT -> {
                event.setCancelled(true);
                commit();
            }
            case SLOT_BRAND -> {
                // Only a Brand may be placed here.
                if (!isEmpty(cursor) && !CreateCircesBrand.isCircesBrand(cursor)) {
                    event.setCancelled(true);
                }
            }
            case SLOT_TOOL -> {
                if (!isEmpty(cursor) && (!isRenameable(cursor) || CreateCircesBrand.isCircesBrand(cursor))) {
                    event.setCancelled(true);
                }
            }
            default -> event.setCancelled(true);
        }
    }

    /** Send a shift-clicked item to whichever input slot it belongs in. */
    private void routeShiftClick(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (isEmpty(clicked)) return;

        int target;
        if (CreateCircesBrand.isCircesBrand(clicked)) {
            target = SLOT_BRAND;
        } else if (isRenameable(clicked)) {
            target = SLOT_TOOL;
        } else {
            return;
        }

        if (inventory.getItem(target) != null) return; // slot already occupied

        // Only ever move a single item - the input slots are one-item slots and a partial move
        // would leave the remainder unaccounted for on close.
        ItemStack single = clicked.clone();
        single.setAmount(1);
        inventory.setItem(target, single);

        if (clicked.getAmount() <= 1) {
            event.setCurrentItem(null);
        } else {
            clicked.setAmount(clicked.getAmount() - 1);
        }
    }

    /**
     * Take the rename: consume one Brand, hand the renamed item to the player and close.
     *
     * The renamed item is given directly rather than left on the cursor, so there is no window
     * in which a half-finished trade can be dropped or duplicated by a close.
     */
    private void commit() {
        ItemStack tool = inventory.getItem(SLOT_TOOL);
        ItemStack brand = inventory.getItem(SLOT_BRAND);
        ItemStack result = inventory.getItem(SLOT_RESULT);

        if (isEmpty(tool) || isEmpty(brand) || isEmpty(result)) return;

        committed = true;

        inventory.setItem(SLOT_TOOL, null);
        inventory.setItem(SLOT_RESULT, null);

        if (brand.getAmount() > 1) {
            // Any Brands beyond the one consumed still belong to the player.
            ItemStack remainder = brand.clone();
            remainder.setAmount(brand.getAmount() - 1);
            giveBack(remainder);
        }
        inventory.setItem(SLOT_BRAND, null);

        giveBack(result);

        playSound("CircesBrandSounds.RenameItem", "BLOCK_ANVIL_USE");
        sendMessages("CircesBrandMessages.RenameItem");

        Bukkit.getScheduler().runTask(main, () -> player.closeInventory());
    }

    // ------------------------------------------------------------------------------------
    // Close
    // ------------------------------------------------------------------------------------

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        if (committed) return;

        // Cancelled: hand both inputs straight back. The result slot holds a preview copy only
        // and is deliberately never returned.
        boolean returnedAnything = false;
        for (int slot : new int[]{SLOT_TOOL, SLOT_BRAND}) {
            ItemStack item = inventory.getItem(slot);
            if (isEmpty(item)) continue;
            giveBack(item);
            inventory.setItem(slot, null);
            returnedAnything = true;
        }
        inventory.setItem(SLOT_RESULT, null);

        if (returnedAnything) {
            sendMessages("CircesBrandMessages.CancelledRename");
        }
    }

    // ------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------

    /** Add to the player's inventory, dropping at their feet if there is no room. */
    private void giveBack(ItemStack item) {
        if (isEmpty(item)) return;
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
        if (isEmpty(item)) return false;
        if (item.getAmount() > 1) return false;

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

    private boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() <= 0;
    }

    private void playSound(String path, String fallback) {
        if (!main.getSettingsConfig().getBoolean(path + ".Toggle", false)) return;
        Sound sound = Main.getSound(main.getSettingsConfig().getString(path + ".Sound", fallback));
        if (sound == null) return;
        float volume = (float) main.getSettingsConfig().getDouble(path + ".Volume", 1);
        float pitch = (float) main.getSettingsConfig().getDouble(path + ".Pitch", 1);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private void sendMessages(String path) {
        if (!main.getSettingsConfig().getBoolean(path + ".Toggle", false)) return;
        for (String msg : main.getSettingsConfig().getStringList(path + ".Message")) {
            player.sendMessage(Main.color(main.setPlaceholders(player, msg)));
        }
    }
}
