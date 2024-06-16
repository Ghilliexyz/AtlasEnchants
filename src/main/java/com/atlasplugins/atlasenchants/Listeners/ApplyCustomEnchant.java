package com.atlasplugins.atlasenchants.Listeners;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ApplyCustomEnchant implements Listener {

    private Main main;
    public ApplyCustomEnchant(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent invEvent) {
        // Check if the clicked inventory is not null and if it's the player's inventory
        if (invEvent.getClickedInventory() != null && invEvent.getClickedInventory().getType() == InventoryType.PLAYER) {
            // Get the player who clicked
            Player player = (Player) invEvent.getWhoClicked();
            // Get the item that was clicked
            ItemStack clickedItem = invEvent.getCurrentItem();
            // Get the item on the cursor
            ItemStack cursorItem = invEvent.getCursor();

            // Ensure that neither the clicked item nor the cursor item is null
            if (clickedItem == null || cursorItem == null) return;

            // Get the custom item material type from the config
            Material customItem = Material.valueOf(main.getConfig().getString("EnchantItems.CustomItem"));

            // If the item on the cursor is not the custom item, return
            if (cursorItem.getType() != customItem) return;

            // Check if the cursor item has metadata
            if (cursorItem.hasItemMeta()) {
                ItemMeta cursorMeta = cursorItem.getItemMeta();
                PersistentDataContainer cursorPDC = cursorMeta.getPersistentDataContainer();
                // Check if the cursor item has custom enchantment data
                if (cursorPDC.has(Main.customEnchantKeys, PersistentDataType.STRING)) {
                    // Get the custom enchantment data from the cursor item
                    String cursorData = cursorPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

                    // Get the metadata of the clicked item
                    ItemMeta itemMeta = clickedItem.getItemMeta();
                    if (itemMeta == null) return; // Ensure itemMeta is not null
                    PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();

                    if (cursorData != null) {
                        // Split the enchantment data into name and level
                        String[] enchantParts = cursorData.split(":");
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);

                        boolean shouldApplyEnchantment = true;
                        // Get the lore of the clicked item, or create a new list if none exists
                        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                        List<String> newEnchantments = new ArrayList<>();

                        // Check if the clicked item already has enchantments
                        if (itemPDC.has(Main.customEnchantKeys, PersistentDataType.STRING)) {
                            String existingEnchantData = itemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);
                            String[] existingEnchantments = existingEnchantData.split(",");

                            // Iterate through existing enchantments
                            for (String existingEnchant : existingEnchantments) {
                                String[] existingEnchantParts = existingEnchant.split(":");
                                if (existingEnchantParts.length < 2) continue; // Ensure correct format
                                String existingEnchantName = existingEnchantParts[0];
                                int existingEnchantLevel = Integer.parseInt(existingEnchantParts[1]);

                                // If the enchantment already exists and is of equal or higher level, do not apply the new one
                                if (existingEnchantName.equals(enchantName)) {
                                    if (existingEnchantLevel >= enchantLevel) {
                                        shouldApplyEnchantment = false;
                                    } else {
                                        // Remove the old enchantment from the lore
                                        lore.removeIf(line -> line.contains(existingEnchantName));
                                    }
                                } else {
                                    newEnchantments.add(existingEnchant);
                                }
                            }
                        }

                        // Apply the enchantment if it's not already present at an equal or higher level
                        if (shouldApplyEnchantment) {
                            newEnchantments.add(cursorData);
                            String updatedEnchantData = String.join(",", newEnchantments);
                            itemPDC.set(Main.customEnchantKeys, PersistentDataType.STRING, updatedEnchantData);

                            // Retrieve list of applicable item types from configuration
                            List<String> applicableItems = main.getConfig().getStringList("Enchantments." + enchantName + ".Enchantment-Apply-Item");

                            // Check if clickedItem type is in the list of applicable items
                            Material clickedItemType = clickedItem.getType();
                            if (applicableItems.contains(clickedItemType.toString())) {
                                // Create the enchantment lore text
                                String enchantLore = Main.color(main.getConfig().getString("Enchantments." + enchantName + ".Enchantment-Apply-Lore")
                                        .replace("{enchantmentName}", enchantName)
                                        .replace("{lvl}", String.valueOf(enchantLevel)));
                                // Add the enchantment lore to the item's lore
                                lore.add(enchantLore);
                                itemMeta.setLore(lore);

                                // Set the updated item meta to the clicked item
                                clickedItem.setItemMeta(itemMeta);

                                // Play sound for when player applies new enchant.
                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f ,1f);

                                // Remove the item from the cursor
                                invEvent.setCursor(new ItemStack(Material.AIR));

                                // Update the inventory to reflect changes
                                player.updateInventory();

                                // Cancel the event to prevent default behavior
                                invEvent.setCancelled(true);

                                System.out.println("Enchantment applied successfully.");
                            } else {
                                System.out.println("Item type does not match the applicable items for this enchantment.");
                            }
                        } else {
                            System.out.println("Item already has the same or higher level enchantment.");
                        }
                    }
                }
            }
        }
    }
}