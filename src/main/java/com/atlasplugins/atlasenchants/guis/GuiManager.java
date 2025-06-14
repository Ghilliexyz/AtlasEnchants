package com.atlasplugins.atlasenchants.guis;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuiManager {

    private static final Map<UUID, Gui> openGuis = new HashMap<>();

    public static Inventory createGui(Player player, String title, int size) {
        return Bukkit.createInventory(null, size, title);
    }

    public static void openGui(Player player, Inventory gui) {
        player.openInventory(gui);
    }

    public static void setOpenGui(Player player, Gui gui) {
        openGuis.put(player.getUniqueId(), gui);
    }

    public static void closeGui(Player player) {
        openGuis.remove(player.getUniqueId());
    }

    public static Gui getOpenGui(Player player) {
        return openGuis.get(player.getUniqueId());
    }

    // Rarity Selection
    private static final Map<UUID, String> raritySelectionMap = new HashMap<>();

    public static void setRarity(UUID playerId, String rarity) {
        raritySelectionMap.put(playerId, rarity);
    }

    public static String getRarity(UUID playerId) {
        return raritySelectionMap.get(playerId);
    }

    public static void clear(UUID playerId) {
        raritySelectionMap.remove(playerId);
    }
}
