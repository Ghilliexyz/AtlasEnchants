package com.atlasplugins.atlasenchants.enchants.defense;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EnergyAbsorption implements Listener {
    private Main main;
    public EnergyAbsorption (Main main) {
        this.main = main;
    }

    public boolean hasDefenseMainHand(Player p) {
        // Get the items in the main hand
        ItemStack mainHandWeapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.ENERGY-ABSORPTION.Enchantment-Apply-Item");

        // Check if the player is holding an applicable item in their hand

        return weaponMat.contains(mainHandWeapon.getType().toString());
    }
    public boolean hasDefenseOffHand(Player p) {
        // Get the items in the off hand
        ItemStack offHandWeapon = p.getInventory().getItemInOffHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.ENERGY-ABSORPTION.Enchantment-Apply-Item");

        // Check if the player is holding an applicable item in their hand

        return weaponMat.contains(offHandWeapon.getType().toString());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if(e.isCancelled()) return;
        if(!(e.getEntity() instanceof Player)) {return;}

        Player p = (Player) e.getEntity();

        // Check if the player has an enchanted sword
        if(hasDefenseMainHand(p) || hasDefenseOffHand(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.ENERGY-ABSORPTION.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;
            // if the player is not blocking return.
            if(!p.isBlocking()) return;

            ItemStack shieldItem = hasDefenseMainHand(p) ? p.getInventory().getItemInMainHand() : p.getInventory().getItemInOffHand();

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(shieldItem)) {
                if (enchant.name.contains("ENERGY-ABSORPTION")) {
                    //PUT ENCHANT LOGIC HERE

                    // get the amount needed before the healing can begin
                    double startHealingAmount = main.getEnchantmentsConfig().getDouble("Enchantments.ENERGY-ABSORPTION.EnergyAbsorption-StartHealAmount-" + enchant.level);

                    // if the players health is greater than 3 return.
                    if(p.getHealth() > (startHealingAmount * 2)) return;

                    // get healing amount
                    double healingAmount = main.getEnchantmentsConfig().getDouble("Enchantments.ENERGY-ABSORPTION.EnergyAbsorption-HealAmount-" + enchant.level);

                    // get the players current health
                    double playersCurrentHealth = p.getHealth();

                    // set the players health to its current health and increase it by the healing amount, capped to max
                    p.setHealth(Math.min(p.getMaxHealth(), playersCurrentHealth + (healingAmount * 2)));

                    // Get the location of the dead entity
                    Location entityLoc = p.getLocation();

                    // Particle Settings Controlled Via Config
                    // Get the bool to see if the user wants to display the particles
                    boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.ENERGY-ABSORPTION.Particle-Settings.Toggle");
                    // Get the Particle 1 Name
                    Particle particle1Name;
                    try {
                        particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.ENERGY-ABSORPTION.Particle-Settings.Particle-1.Name"));
                    } catch (IllegalArgumentException ex) {
                        return;
                    }
                    // Get the Particle 1 Amount
                    int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.ENERGY-ABSORPTION.Particle-Settings.Particle-1.Amount");
                    // Get the Particle 1 Size
                    float particle1Size = (float) main.getEnchantmentsConfig().getDouble("Enchantments.ENERGY-ABSORPTION.Particle-Settings.Particle-1.Size");

                    if(useParticles)
                    {
                        // Spawn particle effect
                        p.getWorld().spawnParticle(particle1Name, entityLoc, particle1Amount, 1, 1, 1, particle1Size);
                    }
                    //END ENCHANT LOGIC
                }
            }
        }
    }
}
