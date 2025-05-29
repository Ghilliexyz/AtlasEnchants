package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlessingofKnowledge implements Listener {

    private Main main;
    public BlessingofKnowledge(Main main) {this.main = main;}

    final HashMap<UUID, String> mobs = new HashMap<>();

    private final Map<UUID, Long> healthBarTimers = new HashMap<>();

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

            PersistentDataContainer container = player.getInventory().getHelmet().getItemMeta().getPersistentDataContainer();
            String enchantmentData = container.getOrDefault(Main.customEnchantKeys, PersistentDataType.STRING, "");

            if (!enchantmentData.isEmpty()) {
                String[] enchantments = enchantmentData.split(",");
                for (String enchantment : enchantments) {
                    String[] enchantParts = enchantment.split(":");
                    // Ensure the format is correct
                    if (enchantParts.length == 3) {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);
                        int enchantID = Integer.parseInt(enchantParts[2]);

                        if (enchantName.contains("BLESSING-OF-KNOWLEDGE")) {
                            LivingEntity hitEntity = (LivingEntity) entity;

                            int healthBarShowTimer = main.getEnchantmentsConfig().getInt("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Timer-" + enchantLevel);
                            int healthBarDurationTicks = healthBarShowTimer * 20; // Convert seconds to ticks

                            String entityName;
                            if (!this.mobs.containsKey(entity.getUniqueId())) {
                                if (hitEntity.getCustomName() != null) {
                                    entityName = hitEntity.getCustomName();
                                } else {
                                    entityName = hitEntity.getName();
                                }
                                this.mobs.put(hitEntity.getUniqueId(), hitEntity.getCustomName());
                                Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this.main, () -> {
                                    hitEntity.setCustomNameVisible(false);
                                    hitEntity.setCustomName(this.mobs.get(hitEntity.getUniqueId()));
                                    this.mobs.remove(hitEntity.getUniqueId());
                                }, healthBarDurationTicks);
                            } else if (this.mobs.get(hitEntity.getUniqueId()) == null) {
                                entityName = hitEntity.getType().toString();
                            } else {
                                entityName = this.mobs.get(hitEntity.getUniqueId());
                            }

                            EntityDamageEvent entityEvent = (EntityDamageEvent) e;
                            double finalDamage = entityEvent.getFinalDamage();

                            // Display health bar
                            hitEntity.setCustomNameVisible(true);

                            double entityHealth = hitEntity.getHealth() - finalDamage;
                            entityHealth = roundToOneDecimalPlace(entityHealth); // Assuming roundToOneDecimalPlace is a method that rounds to one decimal place

                            double healthPercentage = (entityHealth / hitEntity.getMaxHealth()) * 100;

                            int entityMaxHealth = (int) hitEntity.getMaxHealth();

                            String healthStyleBelow10 = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Style-" + enchantLevel + ".HealthBar-Style-Below-10");
                            String healthStyleBelow25 = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Style-" + enchantLevel + ".HealthBar-Style-Below-25");
                            String healthStyleBelow50 = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Style-" + enchantLevel + ".HealthBar-Style-Below-50");
                            String healthStyleBelow75 = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Style-" + enchantLevel + ".HealthBar-Style-Below-75");
                            String healthStyleBelow100 = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Style-" + enchantLevel + ".HealthBar-Style-Below-100");

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
                                        .replace("{entityMaxHealth}", String.valueOf(entityMaxHealth)))); // Dark Red for <= 10%
                            } else if (healthPercentage <= 25.0) {
                                hitEntity.setCustomName(Main.color(healthStyleBelow25PAPISet
                                        .replace("{entityHealth}", String.valueOf(entityHealth))
                                        .replace("{entityMaxHealth}", String.valueOf(entityMaxHealth)))); // Red for <= 25%
                            } else if (healthPercentage <= 50.0) {
                                hitEntity.setCustomName(Main.color(healthStyleBelow50PAPISet
                                        .replace("{entityHealth}", String.valueOf(entityHealth))
                                        .replace("{entityMaxHealth}", String.valueOf(entityMaxHealth)))); // Yellow for <= 50%
                            } else if (healthPercentage <= 75.0) {
                                hitEntity.setCustomName(Main.color(healthStyleBelow75PAPISet
                                        .replace("{entityHealth}", String.valueOf(entityHealth))
                                        .replace("{entityMaxHealth}", String.valueOf(entityMaxHealth)))); // Light Yellow (or another color) for <= 75%
                            } else {
                                hitEntity.setCustomName(Main.color(healthStyleBelow100PAPISet
                                        .replace("{entityHealth}", String.valueOf(entityHealth))
                                        .replace("{entityMaxHealth}", String.valueOf(entityMaxHealth)))); // Green for > 75%
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAnyDamage(EntityDamageEvent e) {
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
            style = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Style-" + enchantLevel + ".HealthBar-Style-Below-10");
        } else if (healthPercent <= 25) {
            style = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Style-" + enchantLevel + ".HealthBar-Style-Below-25");
        } else if (healthPercent <= 50) {
            style = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Style-" + enchantLevel + ".HealthBar-Style-Below-50");
        } else if (healthPercent <= 75) {
            style = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Style-" + enchantLevel + ".HealthBar-Style-Below-75");
        } else {
            style = main.getEnchantmentsConfig().getString("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Style-" + enchantLevel + ".HealthBar-Style-Below-100");
        }

        hitEntity.setCustomNameVisible(true);
        hitEntity.setCustomName(Main.color(
                style.replace("{entityHealth}", String.valueOf(Math.max(0, finalHealth)))
                        .replace("{entityMaxHealth}", String.valueOf((int) maxHealth))
        ));
    }

}
