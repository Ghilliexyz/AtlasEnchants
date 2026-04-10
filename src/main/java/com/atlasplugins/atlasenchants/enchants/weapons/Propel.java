package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
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
        if(e.isCancelled()) return;
        if(!(e.getDamager() instanceof Player)) {return;}

        Player p = (Player) e.getDamager();

        // Check if the player has an enchanted sword
        if(hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.PROPEL.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("PROPEL")) {
                    // PUT ENCHANT LOGIC HERE
                    if (e.getEntity() instanceof LivingEntity entityToLaunch) {
                        // Get the block height from the configuration
                        int blockHeight = main.getEnchantmentsConfig().getInt("Enchantments.PROPEL.Propel-HeightAmount-" + enchant.level);

                        double velocityY = calculateInitialVelocity(blockHeight);

                        // Delay by 1 tick so velocity is applied after vanilla knockback processing
                        main.getServer().getScheduler().runTaskLater(main, () -> {
                            Vector entityVelocity = new Vector(entityToLaunch.getVelocity().getX(), velocityY, entityToLaunch.getVelocity().getZ());
                            entityToLaunch.setVelocity(entityVelocity);
                        }, 1L);
                    }
                    //END ENCHANT LOGIC
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
