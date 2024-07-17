package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.armorevents.ArmorEquipEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;


public class Asclepius implements Listener {

    private Main main;
    public Asclepius(Main main) {this.main = main;}

    public boolean isArmorMaterialValid(ItemStack item) {
        if (item == null) return false;
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.GROWTH.Enchantment-Apply-Item");
        return armorMat.contains(item.getType().toString());
    }

    public boolean hasArmor (Player p) {
        // Get the player's helmet item
        ItemStack armor = p.getInventory().getChestplate();

        if(armor != null)
        {
            p.sendMessage(Main.color("&eHasArmor Check: &f" + armor.getType().toString()));
        }

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.ASCLEPIUS.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        Player p = event.getPlayer();
        p.sendMessage(Main.color("&c----- &6&lArmorEquipEvent EVENT CALLED &c-----"));
        ArmorEquipEvent.ArmorType armorType = event.getArmorType();
        ItemStack equippedArmor = event.getEquippedArmor();
        ItemStack unequippedArmor = event.getUnequippedArmor();

        p.sendMessage(Main.color("&3Armor Type: &f" + armorType.toString()));
        if(equippedArmor != null) {
            p.sendMessage(Main.color("&bEquippedArmor: &f" + equippedArmor.getType().toString()));
        }
        if(unequippedArmor != null)
        {
            p.sendMessage(Main.color("&bUnequippedArmor: &f" + unequippedArmor.getType().toString()));
        }
        p.sendMessage(Main.color("&m&l&c---------------------------------------"));

        // if the armor is not of the correct type return.
        if(!armorType.equals(ArmorEquipEvent.ArmorType.CHESTPLATE)) return;

        // if the armor is not of the correct type then exit the method.
        if(!hasArmor(p)) return;

        // Get the relevant item based on the event type
//        ItemStack relevantItem = (equippedArmor != null && equippedArmor.getType() != Material.AIR) ? equippedArmor : unequippedArmor;
//
//        if (relevantItem == null || relevantItem.getItemMeta() == null) {
//            return;
//        }

//        PersistentDataContainer enchantedItemPDC = relevantItem.getItemMeta().getPersistentDataContainer();
//        p.sendMessage(Main.color("&3Item Found: &f" + relevantItem.getType().toString()));

        PersistentDataContainer enchantedItemPDC = p.getInventory().getChestplate().getItemMeta().getPersistentDataContainer();
        String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

        if(enchantedItemPDC.isEmpty()){
            // Disable The enchant since the new one doesn't have it active on the armor.
            removePlayerMaxHealth(p);
            return;
        }

        if(equippedArmor != null) {
            p.sendMessage(Main.color("&3Equipped Item Found: &f" + equippedArmor.getType().toString()));
        }
        if(unequippedArmor != null) {
            p.sendMessage(Main.color("&3Unequipped Item Found: &f" + unequippedArmor.getType().toString()));
        }
        p.sendMessage(Main.color("&m&l&c---------------------------------------"));

        // Ensure the enchantment data is not null or empty
        String[] enchantments = enchantedItemData.split(",");

        for (String enchantment : enchantments) {
            String[] enchantParts = enchantment.split(":");

            // Ensure the format is correct
            if (enchantParts.length == 2) {
                String enchantName = enchantParts[0];
                int enchantLevel = Integer.parseInt(enchantParts[1]);

                if (enchantName.contains("ASCLEPIUS")) {
                    // PUT ENCHANT LOGIC HERE
                    int healthBoostLevel = main.getEnchantmentsConfig().getInt("Enchantments.ASCLEPIUS.Asclepius-HealthBoost-" + enchantLevel);

                    if (equippedArmor != null && !equippedArmor.getType().equals(Material.AIR)) {
                        // Player equipped new armor
                        p.sendMessage(Main.color("&2You equipped: &f" + equippedArmor.getType().toString()));

                        setPlayerMaxHealth(p, healthBoostLevel);

                        p.sendMessage(Main.color("&m&l&c---------------------------------------"));
                        // Handle any effects or logic related to equipping armor
                    } else if (unequippedArmor != null && !unequippedArmor.getType().equals(Material.AIR)) {
                        // Player unequipped armor
                        p.sendMessage(Main.color("&4You unequipped: &f" + unequippedArmor.getType().toString()));

                        removePlayerMaxHealth(p);

                        p.sendMessage(Main.color("&m&l&c---------------------------------------"));
                        // Handle any effects or logic related to unequipping armor
                    }
                    // END ENCHANT LOGIC
                }
            }
        }
    }

    private void setPlayerMaxHealth(Player p, int level) {
        p.sendMessage(Main.color("&aAdded Player's Max Health: &f" + p.getMaxHealth()));

        // Create the potion effect
        PotionEffect potionType = new PotionEffect(PotionEffectType.HEALTH_BOOST, Integer.MAX_VALUE, level - 1, false, false, true);

        // Apply the potion effect to the entity
        p.addPotionEffect(potionType);

        p.sendMessage(Main.color("&2Player's NEW Max Health: &f" + p.getMaxHealth()));
    }

    private void removePlayerMaxHealth(Player p) {
        p.sendMessage(Main.color("&cRemoved Player's Max Health: &f" + p.getMaxHealth()));

        // Create the potion effect
        PotionEffectType potionType = PotionEffectType.HEALTH_BOOST;

        // Apply the potion effect to the entity
        p.removePotionEffect(potionType);

        p.sendMessage(Main.color("&4Player's NEW Max Health: &f" + p.getMaxHealth()));
    }
}
