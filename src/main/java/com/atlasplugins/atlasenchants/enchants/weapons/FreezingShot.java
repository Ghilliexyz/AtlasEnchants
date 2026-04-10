package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class FreezingShot implements Listener {

    private Main main;

    public FreezingShot(Main main) {
        this.main = main;
    }

    public boolean hasWeapon(Player p) {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();
        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.FREEZING-SHOT.Enchantment-Apply-Item");
        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;
        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getDamager();

        // Check if the player has an enchanted sword
        if (hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.FREEZING-SHOT.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("FREEZING-SHOT")) {
                    //PUT ENCHANT LOGIC HERE
                    // get the item in the player hand
                    Material itemInHand = p.getInventory().getItemInMainHand().getType();
                    // return if user tries to hit the entity with their bow
                    if(itemInHand == Material.BOW || itemInHand == Material.CROSSBOW) {return;}
                    ApplyFreezingShotEffect(e.getEntity(), enchant.level, p);
                    //END ENCHANT LOGIC
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if(e.isCancelled()) return;
        if (!(e.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity().getShooter();

        if (hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.FREEZING-SHOT.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("FREEZING-SHOT")) {
                    //PUT ENCHANT LOGIC HERE
                    Entity hitEntity = e.getHitEntity();
                    if(hitEntity == null) return;
                    ApplyFreezingShotEffect(hitEntity, enchant.level, p);
                    //END ENCHANT LOGIC
                }
            }
        }
    }

    private void ApplyFreezingShotEffect(Entity entity, int enchantLevel, Player player) {
        if (entity instanceof LivingEntity) {
            int freezingShotTimer = main.getEnchantmentsConfig().getInt("Enchantments.FREEZING-SHOT.FreezingShot-FreezeTimer-" + enchantLevel);
            int freezeDurationTicks = freezingShotTimer * 20; // Convert seconds to ticks

            ((LivingEntity) entity).setAI(false); // Disable AI or apply the effect

            // Schedule a task to run after freezeDurationTicks ticks
            new BukkitRunnable() {
                @Override
                public void run() {
                    // This code will run after the freeze duration has elapsed
                    if (entity.isValid()) {
                        ((LivingEntity) entity).setAI(true); // Re-enable AI or remove the effect
                    }
                }
            }.runTaskLater(main, freezeDurationTicks);
        }
    }
}
