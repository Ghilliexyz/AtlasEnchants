package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCircesAnvil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Circe's Anvil: a placed anvil that opens the Upgrade Enchant GUI on right-click.
 *
 * <p>The Altar Of Circe marks itself by writing to the placed block's own PersistentDataContainer,
 * but that only works because an enchanting table is a {@link org.bukkit.block.TileState}. An anvil
 * has no block entity, so there is nothing on the block itself to write to.
 *
 * <p>Instead the anvil positions live in the containing <b>chunk's</b> PDC. Chunk PDCs are saved
 * into the world's region files, so this needs no plugin-side JSON, cannot drift out of sync with
 * the world, and survives world copies and rollbacks along with the blocks it describes.
 */
public class CircesAnvilEvent implements Listener {

    // Positions are stored as one comma-separated string per chunk, each entry "x;y;z" in
    // absolute block coordinates.
    private static final String ENTRY_DELIMITER = ",";
    private static final String COORD_DELIMITER = ";";

    private Main main;

    public CircesAnvilEvent(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(Main.customCircesAnvilKeys, PersistentDataType.STRING)) return;

        markCircesAnvil(e.getBlockPlaced());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        if (!isAnvil(block.getType())) return;

        if (!isCircesAnvil(block)) return;

        e.setCancelled(true);

        Player player = e.getPlayer();

        boolean isEnabled = main.getEnchantmentsConfig().getBoolean("CircesAnvil.CircesAnvil-Enabled", true);
        if (!isEnabled) {
            boolean showMessage = main.getSettingsConfig().getBoolean("CircesAnvilMessages.DisabledAnvil.Toggle", false);
            if (showMessage) {
                for (String msg : main.getSettingsConfig().getStringList("CircesAnvilMessages.DisabledAnvil.Message")) {
                    String withPAPISet = main.setPlaceholders(player, msg);
                    player.sendMessage(Main.color(withPAPISet));
                }
            }
            return;
        }

        if (main.getMenusConfig().getBoolean("UpgradeEnchant-Gui.UpgradeEnchant-Menu.Toggle")) {
            main.openUpgradeEnchantGUI(player);
        }

        boolean playSound = main.getSettingsConfig().getBoolean("CircesAnvilSounds.OpenAnvil.Toggle", false);
        if (playSound) {
            Sound sound = Main.getSound(main.getSettingsConfig().getString("CircesAnvilSounds.OpenAnvil.Sound", "BLOCK_ANVIL_USE"));
            float volume = (float) main.getSettingsConfig().getDouble("CircesAnvilSounds.OpenAnvil.Volume", 1);
            float pitch = (float) main.getSettingsConfig().getDouble("CircesAnvilSounds.OpenAnvil.Pitch", 1);
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (!isAnvil(block.getType())) return;

        if (!isCircesAnvil(block)) return;

        event.setDropItems(false);

        CreateCircesAnvil createCircesAnvil = new CreateCircesAnvil(main);
        ItemStack customAnvil = createCircesAnvil.CreateCircesAnvilItem(1, null);
        block.getWorld().dropItemNaturally(block.getLocation(), customAnvil);

        removeCircesAnvil(block);
    }

    // Anvils damage as they are used, so a placed Circe's Anvil can legitimately be any of the
    // three states.
    private static boolean isAnvil(Material type) {
        return type == Material.ANVIL || type == Material.CHIPPED_ANVIL || type == Material.DAMAGED_ANVIL;
    }

    private boolean isCircesAnvil(Block block) {
        return readPositions(block.getChunk()).contains(positionKey(block));
    }

    private void markCircesAnvil(Block block) {
        Chunk chunk = block.getChunk();
        Set<String> positions = readPositions(chunk);
        if (positions.add(positionKey(block))) {
            writePositions(chunk, positions);
        }
    }

    private void removeCircesAnvil(Block block) {
        Chunk chunk = block.getChunk();
        Set<String> positions = readPositions(chunk);
        if (positions.remove(positionKey(block))) {
            writePositions(chunk, positions);
        }
    }

    private static String positionKey(Block block) {
        return block.getX() + COORD_DELIMITER + block.getY() + COORD_DELIMITER + block.getZ();
    }

    private static Set<String> readPositions(Chunk chunk) {
        String raw = chunk.getPersistentDataContainer().get(Main.customCircesAnvilKeys, PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return new LinkedHashSet<>();
        return new LinkedHashSet<>(Arrays.asList(raw.split(ENTRY_DELIMITER)));
    }

    private static void writePositions(Chunk chunk, Set<String> positions) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        // Drop the key entirely once the last anvil in the chunk is gone, so chunks that briefly
        // held one don't carry an empty string forever.
        if (positions.isEmpty()) {
            pdc.remove(Main.customCircesAnvilKeys);
        } else {
            pdc.set(Main.customCircesAnvilKeys, PersistentDataType.STRING, String.join(ENTRY_DELIMITER, positions));
        }
    }
}
