package com.atlasplugins.atlasenchants.Enchants.Weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Stunning implements Listener {
    private Main main;
    public Stunning (Main main) { this.main = main; }

    private BukkitRunnable particleTask;

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getConfig().getStringList("Enchantments.STUNNING.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e)
    {
        if(!(e.getDamager() instanceof Player)) {return;}

        Player p = (Player) e.getDamager();

        Entity entity = e.getEntity();

        // Check if the player has an enchanted sword
        if(hasWeapon(p)) {
            PersistentDataContainer enchantedItemPDC = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
            String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

            // Ensure the enchantment data is not null or empty
            if (enchantedItemData != null && !enchantedItemData.isEmpty()) {
                String[] enchantments = enchantedItemData.split(",");

                for (String enchantment : enchantments) {
                    String[] enchantParts = enchantment.split(":");

                    // Ensure the format is correct
                    if (enchantParts.length == 2)
                    {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);

                        if (enchantName.contains("STUNNING")) {

                            //PUT ENCHANT LOGIC HERE
                            if (entity instanceof LivingEntity) {
                                int poisonTimer = main.getConfig().getInt("Enchantments.STUNNING.Stunning-Stun-Timer-" + enchantLevel);
                                int poisonLevel = main.getConfig().getInt("Enchantments.STUNNING.Stunning-Stun-Levels-" + enchantLevel);

                                int finalPoisonTimer = poisonTimer * 20;

                                // Create the Slowness potion effect
                                PotionEffect potionTypeSlowness = new PotionEffect(PotionEffectType.getByName("SLOWNESS"), finalPoisonTimer, poisonLevel, false, false, true);
                                // Create the Weakness potion effect
                                PotionEffect potionTypeWeakness = new PotionEffect(PotionEffectType.WEAKNESS, finalPoisonTimer, poisonLevel, false, false, true);

                                // Apply the potion effect to the entity
                                ((LivingEntity) entity).addPotionEffect(potionTypeSlowness);
                                ((LivingEntity) entity).addPotionEffect(potionTypeWeakness);

                                // Particle Settings Controlled Via Config
                                // Get the bool to see if the user wants to display the particles
                                boolean useParticles = main.getConfig().getBoolean("Enchantments.STUNNING.Stunning-Particle-Settings.Stunning-Particle-Toggle");
                                // Get the Particle 1 Name
                                Particle particle1Name = Particle.valueOf(main.getConfig().getString("Enchantments.STUNNING.Stunning-Particle-Settings.Stunning-Particle-1.Stunning-Particle-Name-1"));
                                // Get the Particle 1 Amount
                                int particle1Amount = main.getConfig().getInt("Enchantments.STUNNING.Stunning-Particle-Settings.Stunning-Particle-1.Stunning-Particle-Amount-1");
                                // Get the Particle 1 Size
                                int particle1Size = main.getConfig().getInt("Enchantments.STUNNING.Stunning-Particle-Settings.Stunning-Particle-1.Stunning-Particle-Size-1");

                                if(useParticles) {
                                    stopParticleLoop(); // Reset the Particle Loop

                                    particleTask = new BukkitRunnable() {
                                        int count = 0;

                                        @Override
                                        public void run() {
                                            if (count >= poisonTimer) {
                                                cancel(); // Stop the task after maxCount iterations
                                                return;
                                            }

                                            // Update location in case entity moves
                                            Location entityLoc = entity.getLocation();

                                            // Spawn particle effect
                                            entity.getWorld().spawnParticle(particle1Name, entityLoc, particle1Amount, 1, 1, 1, particle1Size);

                                            count++;
                                        }
                                    };

                                    particleTask.runTaskTimer(main, 0L, 20L); // 0L means start immediately, 20L means run every 1 second (20 ticks)
                                }
                            }
                            //END ENCHANT LOGIC
                        }
                    }else {
                        // Handle unexpected format
                        System.out.println("Unexpected enchantment format: " + enchantment);
                    }
                }
            } else {
                System.out.println("No enchantments found on the item.");
            }
        }
    }

    // Method to stop particle task
    private void stopParticleLoop() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }
}
