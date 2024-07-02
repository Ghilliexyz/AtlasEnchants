package com.atlasplugins.atlasenchants.enchants.weapons;

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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class IceAspect implements Listener {
    private Main main;
    public IceAspect (Main main) { this.main = main; }

    private BukkitRunnable particleTask;

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.ICE-ASPECT.Enchantment-Apply-Item");

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

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.ICE-ASPECT.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            PersistentDataContainer enchantedItemPDC = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
            String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

            // Ensure the enchantment data is not null or empty
            if (enchantedItemData != null && !enchantedItemData.isEmpty()) {
                String[] enchantments = enchantedItemData.split(",");

                for (String enchantment : enchantments) {
                    String[] enchantParts = enchantment.split(":");

                    // Ensure the format is correct
                    if (enchantParts.length == 2) {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);

                        if (enchantName.contains("ICE-ASPECT")) {

                            //PUT ENCHANT LOGIC HERE
                            if (entity instanceof LivingEntity) {
                                int frozenTimer = main.getEnchantmentsConfig().getInt("Enchantments.ICE-ASPECT.IceAspect-Frozen-Timer-" + enchantLevel);

                                int finalFrozenTimer = frozenTimer * 20;

                                entity.setFreezeTicks(finalFrozenTimer * 2);

                                // Particle Settings Controlled Via Config
                                // Get the bool to see if the user wants to display the particles
                                boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.ICE-ASPECT.IceAspect-Particle-Settings.IceAspect-Particle-Toggle");
                                // Get the Particle 1 Name
                                Particle particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.ICE-ASPECT.IceAspect-Particle-Settings.IceAspect-Particle-1.IceAspect-Particle-Name-1"));
                                // Get the Particle 1 Amount
                                int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.ICE-ASPECT.IceAspect-Particle-Settings.IceAspect-Particle-1.IceAspect-Particle-Amount-1");
                                // Get the Particle 1 Size
                                int particle1Size = main.getEnchantmentsConfig().getInt("Enchantments.ICE-ASPECT.IceAspect-Particle-Settings.IceAspect-Particle-1.IceAspect-Particle-Size-1");

                                if(useParticles) {
                                    stopParticleLoop(); // Reset the Particle Loop

                                    particleTask = new BukkitRunnable() {
                                        int count = 0;

                                        @Override
                                        public void run() {
                                            if (count >= frozenTimer) {
                                                cancel(); // Stop the task after maxCount iterations
                                                return;
                                            }

                                            // Update location in case entity moves
                                            Location entityLoc = entity.getLocation();

                                            // Spawn particle effect
                                            entity.getWorld().spawnParticle(particle1Name, entityLoc, particle1Amount, 1, 1, 1, particle1Size);

                                            // check if the entity has died and if so then stop the particles
                                            if(((LivingEntity) entity).getHealth() <= 0)
                                            {
                                                stopParticleLoop();
                                            }

                                            count++;
                                        }
                                    };

                                    particleTask.runTaskTimer(main, 0L, 20L); // 0L means start immediately, 20L means run every 1 second (20 ticks)
                                }
                            }
                            //END ENCHANT LOGIC
                        }
                    }
                }
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
