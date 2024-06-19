package com.atlasplugins.atlasenchants.Enchants.Armor;

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

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getDamager();

        // Check if the player has an enchanted helmet
        if (hasArmor(player)) {
            ItemStack helmet = player.getInventory().getHelmet();
            if (helmet == null) {
                return;
            }

            PersistentDataContainer container = helmet.getItemMeta().getPersistentDataContainer();
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

                            int healthBarShowTimer = main.getConfig().getInt("Enchantments.BLESSING-OF-KNOWLEDGE.Healthbar-Timer-" + enchantLevel);
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
                            entity.setCustomName(Main.color("&a" + (entity.getHealth() - finalDamage) + "/" + entity.getMaxHealth()));
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

    private boolean hasArmor(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null) {
            return false;
        }
        List<String> armorMat = main.getConfig().getStringList("Enchantments.BLESSING-OF-KNOWLEDGE.Enchantment-Apply-Item");
        return armorMat.contains(helmet.getType().toString());
    }
}
