package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BlessingofKnowledge implements Listener {

    private Main main;
    public BlessingofKnowledge(Main main) {this.main = main;}

    final HashMap<UUID, String> mobs = new HashMap<>();

    public double roundToOneDecimalPlace(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private boolean hasArmor(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null) {
            return false;
        }
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.BLESSING-OF-KNOWLEDGE.Enchantment-Apply-Item");
        return armorMat.contains(helmet.getType().toString());
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent e) {
        if(e.isCancelled()) return;
        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getDamager();

        Entity entity = e.getEntity();

        // Return if the entity is a Fireball
        if(entity instanceof Fireball){return;}

        // Return if the entity is a ShulkerBullet
        if(entity instanceof ShulkerBullet) return;

        // Return if the entity is a BreezeWindCharge
        if(entity instanceof BreezeWindCharge) return;

        // Check if the player has an enchanted helmet
        if (hasArmor(player)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.BLESSING-OF-KNOWLEDGE.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(player.getInventory().getHelmet())) {
                if (enchant.name.contains("BLESSING-OF-KNOWLEDGE")) {
                    if(!(entity instanceof LivingEntity)) return;
                    LivingEntity hitEntity = (LivingEntity) entity;

                    int healthBarShowTimer = main.getEnchantmentsConfig().getInt("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthTimer-" + enchant.level);
                    int healthBarDurationTicks = healthBarShowTimer * 20; // Convert seconds to ticks

                    if (!this.mobs.containsKey(hitEntity.getUniqueId())) {
                        // Store original custom name (null for unnamed mobs) so we can restore it later
                        this.mobs.put(hitEntity.getUniqueId(), hitEntity.getCustomName());
                        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this.main, () -> {
                            if (hitEntity.isValid()) {
                                hitEntity.setCustomNameVisible(false);
                                // Restore original name (null clears it for unnamed mobs, nametag text restores for named mobs)
                                hitEntity.setCustomName(this.mobs.get(hitEntity.getUniqueId()));
                            }
                            this.mobs.remove(hitEntity.getUniqueId());
                        }, healthBarDurationTicks);
                    }

                    EntityDamageEvent entityEvent = (EntityDamageEvent) e;
                    double finalDamage = entityEvent.getFinalDamage();

                    // Display health bar
                    hitEntity.setCustomNameVisible(true);

                    double entityHealth = hitEntity.getHealth() - finalDamage;
                    entityHealth = roundToOneDecimalPlace(entityHealth);

                    double healthPercentage = (entityHealth / hitEntity.getMaxHealth()) * 100;

                    int entityMaxHealth = (int) hitEntity.getMaxHealth();

                    String healthStyleBelow10 = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthStyle-" + enchant.level + ".Below-10");
                    String healthStyleBelow25 = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthStyle-" + enchant.level + ".Below-25");
                    String healthStyleBelow50 = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthStyle-" + enchant.level + ".Below-50");
                    String healthStyleBelow75 = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthStyle-" + enchant.level + ".Below-75");
                    String healthStyleBelow100 = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthStyle-" + enchant.level + ".Below-100");

                    String healthStyleBelow10PAPISet = main.setPlaceholders(player, healthStyleBelow10);
                    String healthStyleBelow25PAPISet = main.setPlaceholders(player, healthStyleBelow25);
                    String healthStyleBelow50PAPISet = main.setPlaceholders(player, healthStyleBelow50);
                    String healthStyleBelow75PAPISet = main.setPlaceholders(player, healthStyleBelow75);
                    String healthStyleBelow100PAPISet =  main.setPlaceholders(player, healthStyleBelow100);

                    if(entityHealth <= 0)
                    {
                        entityHealth = 0;
                    }

                    if(healthPercentage <= 10.0) {
                        hitEntity.setCustomName(Main.color(healthStyleBelow10PAPISet
                                .replace("{entityHealth}", String.valueOf(entityHealth))
                                .replace("{entityMaxHealth}", String.valueOf(entityMaxHealth))));
                    } else if (healthPercentage <= 25.0) {
                        hitEntity.setCustomName(Main.color(healthStyleBelow25PAPISet
                                .replace("{entityHealth}", String.valueOf(entityHealth))
                                .replace("{entityMaxHealth}", String.valueOf(entityMaxHealth))));
                    } else if (healthPercentage <= 50.0) {
                        hitEntity.setCustomName(Main.color(healthStyleBelow50PAPISet
                                .replace("{entityHealth}", String.valueOf(entityHealth))
                                .replace("{entityMaxHealth}", String.valueOf(entityMaxHealth))));
                    } else if (healthPercentage <= 75.0) {
                        hitEntity.setCustomName(Main.color(healthStyleBelow75PAPISet
                                .replace("{entityHealth}", String.valueOf(entityHealth))
                                .replace("{entityMaxHealth}", String.valueOf(entityMaxHealth))));
                    } else {
                        hitEntity.setCustomName(Main.color(healthStyleBelow100PAPISet
                                .replace("{entityHealth}", String.valueOf(entityHealth))
                                .replace("{entityMaxHealth}", String.valueOf(entityMaxHealth))));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAnyDamage(EntityDamageEvent e) {
        if(e.isCancelled()) return;
        if (!(e.getEntity() instanceof LivingEntity hitEntity)) return;

        UUID uuid = hitEntity.getUniqueId();

        // Only update if the entity's health bar is currently active
        if (!mobs.containsKey(uuid)) return;

        // Calculate remaining health after damage
        double finalHealth = roundToOneDecimalPlace(hitEntity.getHealth() - e.getFinalDamage());
        double maxHealth = hitEntity.getMaxHealth();
        double healthPercent = (finalHealth / maxHealth) * 100;

        // Re-use the same logic to update the health bar display
        int enchantLevel = 1; // Default or retrieve from somewhere if needed
        String style;
        if (healthPercent <= 10) {
            style = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthStyle-" + enchantLevel + ".Below-10");
        } else if (healthPercent <= 25) {
            style = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthStyle-" + enchantLevel + ".Below-25");
        } else if (healthPercent <= 50) {
            style = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthStyle-" + enchantLevel + ".Below-50");
        } else if (healthPercent <= 75) {
            style = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthStyle-" + enchantLevel + ".Below-75");
        } else {
            style = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.BlessingOfKnowledge-HealthStyle-" + enchantLevel + ".Below-100");
        }

        hitEntity.setCustomNameVisible(true);
        hitEntity.setCustomName(Main.color(
                style.replace("{entityHealth}", String.valueOf(Math.max(0, finalHealth)))
                        .replace("{entityMaxHealth}", String.valueOf((int) maxHealth))
        ));
    }

}
