package com.atlasplugins.atlasenchants.utils;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnchantUtils {

    public static class EnchantData {
        public final String name;
        public final int level;
        public final int id;

        public EnchantData(String name, int level, int id) {
            this.name = name;
            this.level = level;
            this.id = id;
        }
    }

    /**
     * Parses custom enchantment data from an item's PersistentDataContainer.
     * Handles null items, null meta, null/empty PDC data, and malformed entries.
     */
    public static List<EnchantData> parseEnchants(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return Collections.emptyList();

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String data = pdc.getOrDefault(Main.customEnchantKeys, PersistentDataType.STRING, "");

        if (data.isEmpty()) return Collections.emptyList();

        List<EnchantData> result = new ArrayList<>();
        String[] enchantments = data.split(",");
        for (String enchantment : enchantments) {
            String[] parts = enchantment.split(":");
            if (parts.length == 3) {
                try {
                    result.add(new EnchantData(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return result;
    }

    /**
     * Gets the level of a specific enchantment on an item.
     * Returns -1 if the enchantment is not found.
     */
    public static int getEnchantLevel(ItemStack item, String enchantName) {
        for (EnchantData enchant : parseEnchants(item)) {
            if (enchant.name.contains(enchantName)) {
                return enchant.level;
            }
        }
        return -1;
    }
}
