package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCircesAnvil;
import com.atlasplugins.atlasenchants.managers.CircesAnvilManager;
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

public class CircesAnvilEvent implements Listener {

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

        Block block = e.getBlockPlaced();
        main.getCircesAnvilManager().markCircesAnvil(block);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        Material type = block.getType();
        if (type != Material.ANVIL && type != Material.CHIPPED_ANVIL && type != Material.DAMAGED_ANVIL) return;

        if (!main.getCircesAnvilManager().isCircesAnvil(block)) return;

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
            Sound sound = Sound.valueOf(main.getSettingsConfig().getString("CircesAnvilSounds.OpenAnvil.Sound", "BLOCK_ANVIL_USE"));
            float volume = (float) main.getSettingsConfig().getDouble("CircesAnvilSounds.OpenAnvil.Volume", 1);
            float pitch = (float) main.getSettingsConfig().getDouble("CircesAnvilSounds.OpenAnvil.Pitch", 1);
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        Material type = block.getType();
        if (type != Material.ANVIL && type != Material.CHIPPED_ANVIL && type != Material.DAMAGED_ANVIL) return;

        if (!main.getCircesAnvilManager().isCircesAnvil(block)) return;

        event.setDropItems(false);

        CreateCircesAnvil createCircesAnvil = new CreateCircesAnvil(main);
        ItemStack customAnvil = createCircesAnvil.CreateCircesAnvilItem(1, null);
        block.getWorld().dropItemNaturally(block.getLocation(), customAnvil);

        main.getCircesAnvilManager().removeCircesAnvil(block);
    }
}
