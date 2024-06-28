package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getDamager();

        // Check if the player has an enchanted sword
        if (hasWeapon(p)) {
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

                        if (enchantName.contains("FREEZING-SHOT")) {
                            //PUT ENCHANT LOGIC HERE
                            // get the item in the player hand
                            Material itemInHand = p.getInventory().getItemInMainHand().getType();
                            // return if user tries to hit the entity with their bow
                            if(itemInHand == Material.BOW || itemInHand == Material.CROSSBOW) {return;}
                            ApplyFreezingShotEffect(e.getEntity(), enchantLevel, p);
                            //END ENCHANT LOGIC
                        }
                    } else {
                        // Handle unexpected format
                        System.out.println("Unexpected enchantment format: " + enchantment);
                    }
                }
            } else {
                System.out.println("No enchantments found on the item.");
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

                        if (enchantName.contains("FREEZING-SHOT")) {
                            //PUT ENCHANT LOGIC HERE
                            Entity hitEntity = e.getHitEntity();
                            ApplyFreezingShotEffect(hitEntity, enchantLevel, p);
                            //END ENCHANT LOGIC
                        }
                    } else {
                        // Handle unexpected format
                        System.out.println("Unexpected enchantment format: " + enchantment);
                    }
                }
            } else {
                System.out.println("No enchantments found on the item.");
            }
        }
    }

    private void ApplyFreezingShotEffect(Entity entity, int enchantLevel, Player player) {
        if (entity instanceof LivingEntity) {
            int freezingShotTimer = main.getEnchantmentsConfig().getInt("Enchantments.FREEZING-SHOT.FreezingShot-Freeze-Timer-" + enchantLevel);
            int freezeDurationTicks = freezingShotTimer * 20; // Convert seconds to ticks

            ((LivingEntity) entity).setAI(false); // Disable AI or apply the effect

            player.sendMessage(Main.color("&aEntity: &7" + entity));
            player.sendMessage(Main.color("&cTimer: &7" + freezingShotTimer + " seconds"));

            // Schedule a task to run after freezeDurationTicks ticks
            new BukkitRunnable() {
                @Override
                public void run() {
                    // This code will run after the freeze duration has elapsed
                    ((LivingEntity) entity).setAI(true); // Re-enable AI or remove the effect

                    player.sendMessage(Main.color("&aEntity: &7" + entity));
                    player.sendMessage(Main.color("&cFreezing Shot effect has ended."));
                }
            }.runTaskLater(main, freezeDurationTicks);
        }
    }
}
