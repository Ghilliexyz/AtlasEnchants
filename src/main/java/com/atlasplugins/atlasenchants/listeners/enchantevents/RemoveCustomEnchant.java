package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class RemoveCustomEnchant implements Listener {

    private Main main;
    public RemoveCustomEnchant(Main main) {this.main = main;}

    public void RemoveEnchantment(ItemStack item, String enchantName) {
        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String enchantmentsData = pdc.get(Main.customEnchantKeys, PersistentDataType.STRING);

        if (enchantmentsData != null && !enchantmentsData.isEmpty()) {
            StringBuilder newEnchantments = new StringBuilder();
            String[] enchantments = enchantmentsData.split(",");

            for (String enchantment : enchantments) {
                String[] enchantParts = enchantment.split(":");

                if (enchantParts.length == 2) {
                    String currentEnchantName = enchantParts[0];

                    if (!currentEnchantName.equalsIgnoreCase(enchantName)) {
                        if (newEnchantments.length() > 0) {
                            newEnchantments.append(",");
                        }
                        newEnchantments.append(enchantment);
                    }
                }
            }

            if (newEnchantments.length() > 0) {
                pdc.set(Main.customEnchantKeys, PersistentDataType.STRING, newEnchantments.toString());
            } else {
                pdc.remove(Main.customEnchantKeys);
            }

            String enchantNameReformatted = formatEnchantName(enchantName);

            // Update the item's lore to remove the enchantment
            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                lore.removeIf(line -> line.contains(enchantNameReformatted));
                meta.setLore(lore);
            }

            item.setItemMeta(meta);
        }
    }

    private String formatEnchantName(String enchantName) {
        // Replace periods with spaces
        String formattedName = enchantName.replace('-', ' ');

        // Split the name into words
        String[] words = formattedName.split(" ");

        // Capitalize the first letter of each word
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        // Remove trailing space and return the formatted name
        return result.toString().trim();
    }
}
