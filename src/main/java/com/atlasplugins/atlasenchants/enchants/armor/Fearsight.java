package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Fearsight implements Listener
{

    private Main main;
    public Fearsight (Main main) {
        this.main = main;
    }

    private final Map<UUID, List<Entity>> playerEntityMap = new HashMap<>();

    public boolean hasArmor (Player p) {
        // Get the player's helmet item
        ItemStack armor = p.getInventory().getHelmet();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.FEARSIGHT.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        // Check if the player has an enchanted helmet
        if(hasArmor(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.FEARSIGHT.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getHelmet())) {
                double enchantRadius = main.getEnchantmentsConfig().getInt("Enchantments.FEARSIGHT.Fearsight-GlowRadius-" + enchant.level);

                ChatColor hostileMobColor;
                ChatColor passiveMobColor;
                ChatColor normalMobColor;
                try {
                    hostileMobColor = ChatColor.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FEARSIGHT.Glow-Colours.Hostile").toUpperCase());
                    passiveMobColor = ChatColor.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FEARSIGHT.Glow-Colours.Passive").toUpperCase());
                    normalMobColor = ChatColor.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FEARSIGHT.Glow-Colours.Player").toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return;
                }

                if (enchant.name.contains("FEARSIGHT"))
                {
                    //PUT ENCHANT LOGIC HERE
                    List<Entity> nearbyEntities = p.getNearbyEntities(enchantRadius, enchantRadius, enchantRadius);

                    // Remove glow from entities that were previously glowing but are no longer nearby
                    List<Entity> previousEntities = playerEntityMap.get(p.getUniqueId());
                    if (previousEntities != null) {
                        for (Entity prevEntity : previousEntities) {
                            if (!nearbyEntities.contains(prevEntity)) {
                                try {
                                    main.glowingEntities.unsetGlowing(prevEntity, p);
                                } catch (ReflectiveOperationException ex) {
                                    // Entity may have been removed, ignore
                                }
                            }
                        }
                    }

                    playerEntityMap.put(p.getUniqueId(), nearbyEntities);

                    for (Entity entity : nearbyEntities) {
                        try {
                            if (entity instanceof Monster || entity instanceof Flying || entity instanceof Slime || entity instanceof Boss) {
                                main.glowingEntities.setGlowing(entity, p, hostileMobColor);
                            } else if (entity instanceof Animals || entity instanceof Ambient || entity instanceof WaterMob) {
                                main.glowingEntities.setGlowing(entity, p, passiveMobColor);
                            } else if (entity instanceof Player || entity instanceof Villager || entity instanceof WanderingTrader || entity instanceof IronGolem) {
                                main.glowingEntities.setGlowing(entity, p, normalMobColor);
                            }
                        } catch (ReflectiveOperationException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    //END ENCHANT LOGIC
                }
            }
        } else {
            // If no helmet is worn, remove glow from all entities
            removeGlowForPlayer(p);
        }
    }

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent e) {
        Player player = e.getEntity();
        // Remove glow effect when the player dies
        removeGlowForPlayer(player);
    }

    @EventHandler
    public void PlayerLeaveEvent(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        // Remove glow effect when the player leaves the game
        removeGlowForPlayer(player);
    }

    private void removeGlowForPlayer(Player player) {
        List<Entity> entities = playerEntityMap.remove(player.getUniqueId());
        if (entities == null) return;
        for (Entity entity : entities) {
            try {
                main.glowingEntities.unsetGlowing(entity, player);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
