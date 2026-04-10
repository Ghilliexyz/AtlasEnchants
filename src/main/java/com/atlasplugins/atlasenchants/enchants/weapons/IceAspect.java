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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class IceAspect implements Listener {
    private Main main;
    public IceAspect (Main main) { this.main = main; }

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
        if(e.isCancelled()) return;
        if(!(e.getDamager() instanceof Player)) {return;}

        Player p = (Player) e.getDamager();

        Entity entity = e.getEntity();

        // Check if the player has an enchanted sword
        if(hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.ICE-ASPECT.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("ICE-ASPECT")) {

                    //PUT ENCHANT LOGIC HERE
                    if (entity instanceof LivingEntity) {
                        int frozenTimer = main.getEnchantmentsConfig().getInt("Enchantments.ICE-ASPECT.IceAspect-FreezeTimer-" + enchant.level);

                        int finalFrozenTimer = frozenTimer * 20;

                        entity.setFreezeTicks(finalFrozenTimer * 2);

                        // Particle Settings Controlled Via Config
                        // Get the bool to see if the user wants to display the particles
                        boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.ICE-ASPECT.Particle-Settings.Toggle");

                        Particle particle1Name;
                        try {
                            // Get the Particle 1 Name
                            particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.ICE-ASPECT.Particle-Settings.Particle-1.Name"));
                        } catch (IllegalArgumentException ex) {
                            return;
                        }

                        // Get the Particle 1 Amount
                        int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.ICE-ASPECT.Particle-Settings.Particle-1.Amount");
                        // Get the Particle 1 Size
                        int particle1Size = main.getEnchantmentsConfig().getInt("Enchantments.ICE-ASPECT.Particle-Settings.Particle-1.Size");

                        if(useParticles) {
                            new BukkitRunnable() {
                                int count = 0;

                                @Override
                                public void run() {
                                    if (count >= frozenTimer) {
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
