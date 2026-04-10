package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Hunter implements Listener
{
    private Main main;
    public Hunter(Main main) {
        this.main = main;
    }

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.HUNTER.Enchantment-Apply-Item");

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

        Entity entity = e.getEntity();

        // Return if the entity is a shulker bullet
        if(entity instanceof ShulkerBullet) return;

        // Return if the entity is a BreezeWindCharge
        if(entity instanceof BreezeWindCharge) return;

        // Check if the player has an enchanted sword
        if (hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.HUNTER.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("HUNTER")) {
                    //PUT ENCHANT LOGIC HERE
                    // get the item in the player hand
                    Material itemInHand = p.getInventory().getItemInMainHand().getType();
                    // return if user tries to hit the entity with their bow
                    if(itemInHand == Material.BOW || itemInHand == Material.CROSSBOW) {return;}

                    if(!(entity instanceof LivingEntity)) return;
                    LivingEntity hitEntity = (LivingEntity) entity;
                    ApplyDamage(hitEntity, enchant.level, p);
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

        Entity entity = e.getEntity();

        // Return if the entity is a shulker bullet
        if(entity instanceof ShulkerBullet) return;

        // Return if the entity is a BreezeWindCharge
        if(entity instanceof BreezeWindCharge) return;

        if (hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.HUNTER.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("HUNTER")) {
                    //PUT ENCHANT LOGIC HERE
                    if(e.getHitEntity() == null || !(e.getHitEntity() instanceof LivingEntity)) return;
                    LivingEntity hitEntity = (LivingEntity) e.getHitEntity();

                    ApplyDamage(hitEntity, enchant.level, p);
                    //END ENCHANT LOGIC
                }
            }
        }
    }

    private void ApplyDamage(LivingEntity entity, int enchantLevel, Player player)
    {
        if (entity instanceof Animals || entity instanceof Ambient || entity instanceof WaterMob) {
            double extraDamage = main.getEnchantmentsConfig().getDouble("Enchantments.HUNTER.Hunter-DamageAmount-" + enchantLevel);

            entity.damage(extraDamage);
        }
    }
}
