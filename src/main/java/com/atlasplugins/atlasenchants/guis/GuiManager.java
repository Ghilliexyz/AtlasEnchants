package com.atlasplugins.atlasenchants.guis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GuiManager {
    public static Inventory createGui(Player player, String title, int size) {
        Inventory gui = Bukkit.createInventory(null, size, title);

        // Add default or common items here if necessary

        return gui;
    }

    public static void openGui(Player player, Inventory gui) {
        player.openInventory(gui);
    }
}
