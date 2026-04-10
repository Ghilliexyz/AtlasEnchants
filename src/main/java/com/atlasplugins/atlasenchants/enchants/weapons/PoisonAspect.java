package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PoisonAspect implements Listener {
    private Main main;
    public PoisonAspect (Main main) {
        this.main = main;
    }

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.POISON-ASPECT.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e)
    {
        if(e.isCancelled()) return;
        if(!(e.getDamager() instanceof Player)) {return;}

        Player p = (Player) e.getDamager();

        Entity entity = e.getEntity();

        // Check if the player has an enchanted sword
        if(hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.POISON-ASPECT.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("POISON-ASPECT")) {

                    //PUT ENCHANT LOGIC HERE
                    if (entity instanceof LivingEntity) {

                        int poisonTimer = main.getEnchantmentsConfig().getInt("Enchantments.POISON-ASPECT.PoisonAspect-PoisonTimer-" + enchant.level);
                        int poisonLevel = main.getEnchantmentsConfig().getInt("Enchantments.POISON-ASPECT.PoisonAspect-PoisonLevel-" + enchant.level);

                        int finalPoisonTimer = poisonTimer * 20;

                        // Create the potion effect
                        PotionEffect potionType = new PotionEffect(PotionEffectType.POISON, finalPoisonTimer, poisonLevel - 1, false, false, true);

                        // Apply the potion effect to the entity
                        ((LivingEntity) entity).addPotionEffect(potionType);

                        // Particle Settings Controlled Via Config
                        // Get the bool to see if the user wants to display the particles
                        boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.POISON-ASPECT.Particle-Settings.Toggle");

                        Particle particle1Name;
                        Particle particle2Name;
                        try {
                            // Get the Particle 1 Name
                            particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.POISON-ASPECT.Particle-Settings.Particle-1.Name"));
                            // Get the Particle 2 Name
                            particle2Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.POISON-ASPECT.Particle-Settings.Particle-2.Name"));
                        } catch (IllegalArgumentException ex) {
                            return;
                        }

                        // Get the Particle 1 Amount
                        int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.POISON-ASPECT.Particle-Settings.Particle-1.Amount");
                        // Get the Particle 1 Size
                        float particle1Size = (float) main.getEnchantmentsConfig().getDouble("Enchantments.POISON-ASPECT.Particle-Settings.Particle-1.Size");
                        // Get the Particle 2 Amount
                        int particle2Amount = main.getEnchantmentsConfig().getInt("Enchantments.POISON-ASPECT.Particle-Settings.Particle-2.Amount");
                        // Get the Particle 2 Size
                        float particle2Size = (float) main.getEnchantmentsConfig().getDouble("Enchantments.POISON-ASPECT.Particle-Settings.Particle-2.Size");

                        if(useParticles) {
                            new BukkitRunnable() {
                                int count = 0;

                                @Override
                                public void run() {
                                    if (count >= poisonTimer) {
                                        cancel(); // Stop the task after maxCount iterations
                                        return;
                                    }

                                    // check if the entity has died or been removed
                                    if(!entity.isValid() || ((LivingEntity) entity).isDead()) {
                                        cancel();
                                        return;
                                    }

                                    // Update location in case entity moves
                                    Location entityLoc = entity.getLocation();

                                    // Spawn particle effect
                                    entity.getWorld().spawnParticle(particle1Name, entityLoc, particle1Amount, 1, 1, 1, particle1Size);
                                    entity.getWorld().spawnParticle(particle2Name, entityLoc, particle2Amount, 1, 1, 1, particle2Size);

                                    count++;
                                }
                            }.runTaskTimer(main, 0L, 20L); // 0L means start immediately, 20L means run every 1 second (20 ticks)
                        }
                    }
                    //END ENCHANT LOGIC
                }
            }
        }
    }

}
