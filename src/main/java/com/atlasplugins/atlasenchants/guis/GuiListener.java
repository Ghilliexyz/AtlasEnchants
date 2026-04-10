package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GuiListener implements Listener {

    private Main main;

    public GuiListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Gui gui = GuiManager.getOpenGui(player);
        if (gui != null && event.getView().getTopInventory().equals(gui.getInventory())) {
            gui.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Gui gui = GuiManager.getOpenGui(player);

        if (gui != null && gui.inventory.equals(event.getInventory())) {
            // Player closed their custom GUI
            gui.onInventoryClose(event);
            GuiManager.closeGui(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GuiManager.closeGui(event.getPlayer());
        GuiManager.clear(event.getPlayer().getUniqueId());
    }

//    private int getCurrentPageFromTitle(String title) {
//        // Remove Minecraft color/formatting codes
//        String cleanTitle = title.replaceAll("§[0-9a-fk-orA-FK-OR]", "").replaceAll("&[0-9a-fk-orA-FK-OR]", "");
//
//        String[] parts = cleanTitle.split(" ");
//
//        // Ensure the title has at least two parts and the last part is a number
//        if (parts.length > 1 && isNumeric(parts[parts.length - 1])) {
//            return Integer.parseInt(parts[parts.length - 1]) - 1;
//        }
//
//        // Default to page 0 if no valid page number is found
//        return 0;
//    }
//
//    private boolean isNumeric(String str) {
//        try {
//            Integer.parseInt(str);
//            return true;
//        } catch (NumberFormatException e) {
//            return false;
//        }
//    }
}

