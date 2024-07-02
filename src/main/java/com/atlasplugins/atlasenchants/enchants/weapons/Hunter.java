package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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
        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getDamager();

        // Check if the player has an enchanted sword
        if (hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.HUNTER.Enchantment-Enabled");
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


                        if (enchantName.contains("HUNTER")) {
                            //PUT ENCHANT LOGIC HERE
                            // get the item in the player hand
                            Material itemInHand = p.getInventory().getItemInMainHand().getType();
                            // return if user tries to hit the entity with their bow
                            if(itemInHand == Material.BOW || itemInHand == Material.CROSSBOW) {return;}

                            LivingEntity entity = (LivingEntity) e.getEntity();
                            ApplyDamage(entity, enchantLevel, p);
                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getEntity().getShooter();

        if (hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.HUNTER.Enchantment-Enabled");
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

                        if (enchantName.contains("HUNTER")) {
                            //PUT ENCHANT LOGIC HERE
                            LivingEntity hitEntity = (LivingEntity) e.getHitEntity();

                            ApplyDamage(hitEntity, enchantLevel, p);
                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }

    private void ApplyDamage(LivingEntity entity, int enchantLevel, Player player)
    {
        if (entity instanceof Animals || entity instanceof Ambient || entity instanceof WaterMob) {
            double extraDamage = main.getEnchantmentsConfig().getDouble("Enchantments.HUNTER.Hunter-Damage-Amount-" + enchantLevel);

            entity.damage(extraDamage);
        }
    }
}
