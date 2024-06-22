package com.atlasplugins.atlasenchants.Enchants.Weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
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

public class IceAspect implements Listener {
    private Main main;
    public IceAspect (Main main) {
        this.main = main;
    }

    private BukkitRunnable particleTask;

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getConfig().getStringList("Enchantments.ICE-ASPECT.Enchantment-Apply-Item");

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

                        if (enchantName.contains("ICE-ASPECT")) {

                            //PUT ENCHANT LOGIC HERE
                            if (entity instanceof LivingEntity) {
                                stopParticleLoop();

                                int frozenTimer = main.getConfig().getInt("Enchantments.ICE-ASPECT.FrozenAspect-Frozen-Timer-" + enchantLevel);

                                final int finalFrozenTimer = frozenTimer * 20;

                                entity.setFreezeTicks(finalFrozenTimer * 2);


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
                                        entity.getWorld().spawnParticle(Particle.SNOWFLAKE, entityLoc, 50, 1, 1, 1, 0.1);

                                        count++;
                                    }
                                };

                                particleTask.runTaskTimer(main, 0L, 20L); // 0L means start immediately, 20L means run every 1 second (20 ticks)
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
