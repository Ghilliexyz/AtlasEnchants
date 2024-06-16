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
        if (invEvent.getClickedInventory() != null && invEvent.getClickedInventory().getType() == InventoryType.PLAYER) {
            Player player = (Player) invEvent.getWhoClicked();
            ItemStack clickedItem = invEvent.getCurrentItem();
            ItemStack cursorItem = invEvent.getCursor();

            if (cursorItem != null && cursorItem.hasItemMeta()) {
                ItemMeta cursorMeta = cursorItem.getItemMeta();
                PersistentDataContainer cursorPDC = cursorMeta.getPersistentDataContainer();
                if (cursorPDC.has(Main.customEnchantKeys, PersistentDataType.STRING)) {
                    String enchantData = cursorPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

                    ItemMeta itemMeta = clickedItem.getItemMeta();
                    if(itemMeta == null){return;}
                    PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();

                    if(enchantData != null) {
                        String[] enchantParts = enchantData.split(":");
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);

                        boolean shouldApplyEnchantment = true;
                        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                        List<String> newEnchantments = new ArrayList<>();

                        if (itemPDC.has(Main.customEnchantKeys, PersistentDataType.STRING)) {
                            String existingEnchantData = itemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);
                            String[] existingEnchantments = existingEnchantData.split(",");

                            for (String existingEnchant : existingEnchantments) {
                                String[] existingEnchantParts = existingEnchant.split(":");
                                if (existingEnchantParts.length < 2) continue; // Ensure correct format
                                String existingEnchantName = existingEnchantParts[0];
                                int existingEnchantLevel = Integer.parseInt(existingEnchantParts[1]);

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


                        if (shouldApplyEnchantment) {
                            newEnchantments.add(enchantData);
                            String updatedEnchantData = String.join(",", newEnchantments);
                            itemPDC.set(Main.customEnchantKeys, PersistentDataType.STRING, updatedEnchantData);

                            // Retrieve list of applicable item types from configuration
                            List<String> applicableItems = main.getConfig().getStringList("Enchantments." + enchantName + ".Enchantment-Apply-Item");

                            // Check if clickedItem type is in the list of applicable items
                            Material clickedItemType = clickedItem.getType();
                            if (applicableItems.contains(clickedItemType.toString())) {
                                String enchantLore = Main.color(main.getConfig().getString("Enchantments." + enchantName + ".Enchantment-Apply-Lore")
                                        .replace("{enchantmentName}", enchantName)
                                        .replace("{lvl}", String.valueOf(enchantLevel)));
                                lore.add(enchantLore);
                                itemMeta.setLore(lore);

                                clickedItem.setItemMeta(itemMeta);

                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f ,1f);

                                // Remove the item from the cursor
                                invEvent.setCursor(new ItemStack(Material.AIR));

                                // Update the inventory to reflect changes
                                player.updateInventory();

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