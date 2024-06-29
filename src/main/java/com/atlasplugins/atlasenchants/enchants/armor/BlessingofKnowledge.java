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
import java.util.UUID;
import java.util.logging.Level;

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
        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getDamager();

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
                    String[] parts = enchantment.split(":");
                    if (parts.length == 2) {
                        String enchantName = parts[0];
                        int enchantLevel = Integer.parseInt(parts[1]);

                        if (enchantName.contains("BLESSING-OF-KNOWLEDGE")) {
                            LivingEntity entity = (LivingEntity) e.getEntity();

                            int healthBarShowTimer = main.getEnchantmentsConfig().getInt("Enchantments.BLESSING-OF-KNOWLEDGE.HealthBar-Timer-" + enchantLevel);
                            int healthBarDurationTicks = healthBarShowTimer * 20; // Convert seconds to ticks

                            String entityName;
                            if (!this.mobs.containsKey(entity.getUniqueId())) {
                                if (entity.getCustomName() != null) {
                                    entityName = entity.getCustomName();
                                } else {
                                    entityName = entity.getName();
                                }
                                this.mobs.put(entity.getUniqueId(), entity.getCustomName());
                                Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this.main, () -> {
                                    entity.setCustomNameVisible(false);
                                    entity.setCustomName(this.mobs.get(entity.getUniqueId()));
                                    this.mobs.remove(entity.getUniqueId());
                                }, healthBarDurationTicks);
                            } else if (this.mobs.get(entity.getUniqueId()) == null) {
                                entityName = entity.getType().toString();
                            } else {
                                entityName = this.mobs.get(entity.getUniqueId());
                            }

                            EntityDamageEvent entityEvent = (EntityDamageEvent) e;
                            double finalDamage = entityEvent.getFinalDamage();

                            // Display health bar
                            entity.setCustomNameVisible(true);

                            double entityHealth = entity.getHealth() - finalDamage;
                            entityHealth = roundToOneDecimalPlace(entityHealth); // Assuming roundToOneDecimalPlace is a method that rounds to one decimal place

                            double healthPercentage = (entityHealth / entity.getMaxHealth()) * 100;

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



                            if(healthPercentage <= 10.0) {
                                entity.setCustomName(Main.color(healthStyleBelow10PAPISet
                                        .replace("{entityHealth}", String.valueOf(entityHealth))
                                        .replace("{entityMaxHealth}", String.valueOf(entity.getMaxHealth())))); // Dark Red for <= 10%
                            } else if (healthPercentage <= 25.0) {
                                entity.setCustomName(Main.color(healthStyleBelow25PAPISet
                                        .replace("{entityHealth}", String.valueOf(entityHealth))
                                        .replace("{entityMaxHealth}", String.valueOf(entity.getMaxHealth())))); // Red for <= 25%
                            } else if (healthPercentage <= 50.0) {
                                entity.setCustomName(Main.color(healthStyleBelow50PAPISet
                                        .replace("{entityHealth}", String.valueOf(entityHealth))
                                        .replace("{entityMaxHealth}", String.valueOf(entity.getMaxHealth())))); // Yellow for <= 50%
                            } else if (healthPercentage <= 75.0) {
                                entity.setCustomName(Main.color(healthStyleBelow75PAPISet
                                        .replace("{entityHealth}", String.valueOf(entityHealth))
                                        .replace("{entityMaxHealth}", String.valueOf(entity.getMaxHealth())))); // Light Yellow (or another color) for <= 75%
                            } else {
                                entity.setCustomName(Main.color(healthStyleBelow100PAPISet
                                        .replace("{entityHealth}", String.valueOf(entityHealth))
                                        .replace("{entityMaxHealth}", String.valueOf(entity.getMaxHealth())))); // Green for > 75%
                            }
                        }
                    } else {
                        main.getLogger().log(Level.WARNING, "Unexpected enchantment format: " + enchantment);
                    }
                }
            } else {
                main.getLogger().log(Level.INFO, "No enchantments found on the item.");
            }
        }
    }
}
