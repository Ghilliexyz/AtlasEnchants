package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.armorevents.ArmorEquipEvent;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;


public class Asclepius implements Listener {

    private Main main;
    public Asclepius(Main main) {this.main = main;}

    public boolean isArmorMaterialValid(ItemStack item) {
        if (item == null) return false;
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.ASCLEPIUS.Enchantment-Apply-Item");
        return armorMat.contains(item.getType().toString());
    }

    public boolean hasArmor (Player p) {
        // Get the player's helmet item
        ItemStack armor = p.getInventory().getChestplate();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.ASCLEPIUS.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        Player p = event.getPlayer();
        ArmorEquipEvent.ArmorType armorType = event.getArmorType();
        ItemStack equippedArmor = event.getEquippedArmor();
        ItemStack unequippedArmor = event.getUnequippedArmor();

        // if the armor is not of the correct type then exit the method.
        if(!hasArmor(p)){
            removePlayerMaxHealth(p);
            return;
        }

        // if the armor is not of the correct type return.
        if(!armorType.equals(ArmorEquipEvent.ArmorType.CHESTPLATE)) return;

        // Get Enchantment Enabled Status
        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.ASCLEPIUS.Enchantment-Enabled");
        if(!isEnchantmentEnabled) return;

        for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getChestplate())) {
            if (enchant.name.contains("ASCLEPIUS")) {
                // PUT ENCHANT LOGIC HERE
                int healthBoostLevel = main.getEnchantmentsConfig().getInt("Enchantments.ASCLEPIUS.Asclepius-HealthBoost-" + enchant.level);

                if (equippedArmor != null && !equippedArmor.getType().equals(Material.AIR)) {
                    setPlayerMaxHealth(p, healthBoostLevel);
                } else if (unequippedArmor != null && !unequippedArmor.getType().equals(Material.AIR)) {
                    removePlayerMaxHealth(p);
                }
                // END ENCHANT LOGIC
            }
        }
    }

    private void setPlayerMaxHealth(Player p, int level) {
        PotionEffect potionType = new PotionEffect(PotionEffectType.HEALTH_BOOST, PotionEffect.INFINITE_DURATION, level - 1, false, false, true);
        p.addPotionEffect(potionType);
    }

    private void removePlayerMaxHealth(Player p) {
        p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
    }
}
