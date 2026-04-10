package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Rush implements Listener
{

    private Main main;
    public Rush (Main main) {
        this.main = main;
    }

    public boolean hasArmor (Player p) {
        // Get the player's sword item
        ItemStack armor = p.getInventory().getLeggings();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.RUSH.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if(e.isCancelled()) return;
        if(!(e.getEntity() instanceof Player)) {return;}

        Player p = (Player) e.getEntity();

        // Check if the player has an enchanted sword
        if(hasArmor(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.RUSH.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getLeggings())) {
                if (enchant.name.contains("RUSH"))
                {
                    // PUT ENCHANT LOGIC HERE
                    if (e.getDamageSource() == null) return;
                    Entity damagingEntity = e.getDamageSource().getDirectEntity();
                    if (damagingEntity instanceof LivingEntity)
                    {
                        // Get speed level and duration from the configuration
                        int speedLvl = main.getEnchantmentsConfig().getInt("Enchantments.RUSH.Rush-SpeedAmount-" + enchant.level);
                        int speedTimer = main.getEnchantmentsConfig().getInt("Enchantments.RUSH.Rush-SpeedTimer-" + enchant.level);

                        // Ensure the timer is in ticks (20 ticks = 1 second)
                        speedTimer = speedTimer * 20;

                        // Create the potion effect
                        PotionEffect potionType = new PotionEffect(PotionEffectType.SPEED, speedTimer, speedLvl - 1, true, false, true);

                        // Apply the potion effect to the player
                        p.addPotionEffect(potionType);
                    }
                    //END ENCHANT LOGIC
                }
            }
        }
    }
}
