package com.atlasplugins.atlasenchants.Enchants.Armor;

import com.atlasplugins.atlasenchants.Listeners.ArmorEquipEvent;
import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Growth implements Listener {

    private Main main;
    public Growth(Main main) {this.main = main;}
//
//    public boolean isArmorMaterialValid(ItemStack item) {
//        if (item == null) return false;
//        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.GROWTH.Enchantment-Apply-Item");
//        return armorMat.contains(item.getType().toString());
//    }
//
//    @EventHandler
//    public void onArmorEquip(ArmorEquipEvent event) {
//        Player p = event.getPlayer();
//        ArmorEquipEvent.ArmorType armorType = event.getArmorType();
//        ItemStack equippedArmor = event.getEquippedArmor();
//        ItemStack unequippedArmor = event.getUnequippedArmor();
//        p.sendMessage(Main.color("&5Armor Type: &d" + armorType.toString()));
//
//        // Get the relevant item based on the event type
//        ItemStack relevantItem = (equippedArmor != null && equippedArmor.getType() != Material.AIR) ? equippedArmor : unequippedArmor;
//
//        if (relevantItem == null || relevantItem.getItemMeta() == null) {
//            return;
//        }
//
//        PersistentDataContainer enchantedItemPDC = relevantItem.getItemMeta().getPersistentDataContainer();
//        p.sendMessage(Main.color("&bItem Found: " + relevantItem.getType().toString()));
//
//        p.sendMessage(Main.color("---------------------------------------"));
//        String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);
//
//        // Ensure the enchantment data is not null or empty
//        if (enchantedItemData != null && !enchantedItemData.isEmpty()) {
//            String[] enchantments = enchantedItemData.split(",");
//
//            for (String enchantment : enchantments) {
//                String[] enchantParts = enchantment.split(":");
//
//                // Ensure the format is correct
//                if (enchantParts.length == 2) {
//                    String enchantName = enchantParts[0];
//                    int enchantLevel = Integer.parseInt(enchantParts[1]);
//
//                    if (enchantName.contains("GROWTH")) {
//                        // PUT ENCHANT LOGIC HERE
//                        double extraConfigHealth = main.getEnchantmentsConfig().getDouble("Enchantments.GROWTH.Growth-Heart-Increase-" + enchantLevel);
//
//                        if(equippedArmor.getType() == Material.AIR || unequippedArmor.getType() != Material.AIR){return;}
//
//                        if (equippedArmor != null) {
//                            // Player equipped new armor
//                            double playersMaxHealth = p.getMaxHealth() + (extraConfigHealth * 2);
//                            setPlayerMaxHealth(p, playersMaxHealth);
//
//                            p.sendMessage(Main.color("&6You equipped &e" + equippedArmor.getType().toString()));
//                            p.sendMessage(Main.color("---------------------------------------"));
//                            // Handle any effects or logic related to equipping armor
//                        } else if (unequippedArmor != null) {
//                            // Player unequipped armor
//                            double playersMaxHealth = p.getMaxHealth() - (extraConfigHealth * 2);
//                            setPlayerMaxHealth(p, playersMaxHealth);
//
//                            p.sendMessage(Main.color("&4You unequipped &c" + unequippedArmor.getType().toString()));
//                            p.sendMessage(Main.color("---------------------------------------"));
//                            // Handle any effects or logic related to unequipping armor
//                        }
//                        // END ENCHANT LOGIC
//                    }
//                } else {
//                    // Handle unexpected format
//                    System.out.println("Unexpected enchantment format: " + enchantment);
//                }
//            }
//        } else {
//            System.out.println("No enchantments found on the item.");
//        }
//    }
//
//    private void setPlayerMaxHealth(Player p, double newMaxHealth) {
//        p.sendMessage(Main.color("&3Player's OLD Max health: &a" + p.getMaxHealth()));
//
//        p.setMaxHealth(newMaxHealth);
//        p.setHealth(newMaxHealth);
//
//        p.sendMessage(Main.color("&bPlayer's NEW Max health: &a" + p.getMaxHealth()));
//    }
}
