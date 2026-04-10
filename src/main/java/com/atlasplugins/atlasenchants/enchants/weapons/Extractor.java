package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Extractor implements Listener {
    private Main main;
    public Extractor (Main main) {
        this.main = main;
    }

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.EXTRACTOR.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent e)
    {
        Player p = (Player) e.getEntity().getKiller();

        if(p == null){return;}

        // Check if the player has an enchanted sword
        if(hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.EXTRACTOR.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("EXTRACTOR")) {
                    //PUT ENCHANT LOGIC HERE
                    double expMultiplier = main.getEnchantmentsConfig().getDouble("Enchantments.EXTRACTOR.Extractor-ExpMultiplier-" + enchant.level);

                    int droppedEXP = e.getDroppedExp();

                    int finalEXP = (int) (droppedEXP * expMultiplier);

                    e.setDroppedExp(finalEXP);

                    // Get the location of the dead entity
                    Location entityLoc = e.getEntity().getLocation();

                    // Particle Settings Controlled Via Config
                    // Get the bool to see if the user wants to display the particles
                    boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.EXTRACTOR.Particle-Settings.Toggle");
                    // Get the Particle 1 Name
                    Particle particle1Name;
                    try {
                        particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.EXTRACTOR.Particle-Settings.Particle-1.Name"));
                    } catch (IllegalArgumentException ex) {
                        return;
                    }
                    // Get the Particle 1 Amount
                    int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.EXTRACTOR.Particle-Settings.Particle-1.Amount");
                    // Get the Particle 1 Size
                    float particle1Size = (float) main.getEnchantmentsConfig().getDouble("Enchantments.EXTRACTOR.Particle-Settings.Particle-1.Size");

                    if(useParticles)
                    {
                        // Spawn particle effect
                        e.getEntity().getWorld().spawnParticle(particle1Name, entityLoc, particle1Amount, 1, 1, 1, particle1Size);
                    }
                    //END ENCHANT LOGIC
                }
            }
        }
    }
}
