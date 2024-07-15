package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.List;

public class Propel implements Listener
{

    private Main main;
    public Propel (Main main) {
        this.main = main;
    }

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.PROPEL.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e)
    {
        if(!(e.getDamager() instanceof Player)) {return;}

        Player p = (Player) e.getDamager();

        // Check if the player has an enchanted sword
        if(hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.PROPEL.Enchantment-Enabled");
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
                    if (enchantParts.length == 2)
                    {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);

                        if (enchantName.contains("PROPEL")) {
                            // PUT ENCHANT LOGIC HERE
                            if (e.getEntity() instanceof LivingEntity) {
                                // Get the block height from the configuration
                                int blockHeight = main.getEnchantmentsConfig().getInt("Enchantments.PROPEL.Propel-Height-Amount-" + enchantLevel);

                                // Get the entity to launch
                                LivingEntity entityToLaunch = (LivingEntity) e.getEntity();

                                // Default Jump Height = 1.25220 (same as 0.42 Velocity)
                                // Velocity: 0.42 = 1.25220
                                // Velocity: 0.40 = 1.15311

                                double velocityY = calculateInitialVelocity(blockHeight);

                                double entityDamage = e.getDamage();

                                // Add a small vertical offset to the entity's position to ensure it gets launched
//                                Vector entityVelocity = new Vector(entityToLaunch.getVelocity().getX(), velocityY, entityToLaunch.getVelocity().getZ());
                                Vector entityVelocity = new Vector(0, velocityY, 0);
                                // Launch Entity Up
                                entityToLaunch.setVelocity(entityVelocity);
                                // Give The Entity the correct damage since e.setCancelled(true); cancels the damage as well.
                                entityToLaunch.damage(entityDamage);

                                // check if the entity has died and if so then stop the particles
//                                if(entityToLaunch.getHealth() <= 0)
//                                {
//                                    ExperienceOrb experienceOrb = entityToLaunch.getWorld().spawn(entityToLaunch.getLocation(), ExperienceOrb.class);
//                                    experienceOrb.setExperience((int) onEntityDeath((EntityDeathEvent) entityToLaunch));
//                                }

                                e.setCancelled(true);
                            }
                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }

    // Method to calculate initial jump velocity for a given block height
    public static double calculateInitialVelocity(double blockHeight) {
        double gravity = 0.08; // Gravity in Minecraft
        return Math.sqrt(2 * gravity * blockHeight);
    }
}
