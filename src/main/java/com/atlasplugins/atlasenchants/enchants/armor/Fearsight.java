package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Fearsight implements Listener
{

    private Main main;
    public Fearsight (Main main) {
        this.main = main;
        startGlowTask();
    }

    // Tracks which colour each entity is currently glowing for a given player, so we only
    // re-send a glow packet when the colour actually changes (e.g. a wolf turning aggressive).
    private final Map<UUID, Map<Entity, ChatColor>> playerEntityMap = new HashMap<>();

    public boolean hasArmor (Player p) {
        // Get the player's helmet item
        ItemStack armor = p.getInventory().getHelmet();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.FEARSIGHT.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    /**
     * Drives the glow refresh on a fixed interval instead of per movement.
     *
     * <p>This used to run from PlayerMoveEvent, which fires on head rotation as well as movement -
     * many times per tick per player - and each call did a getNearbyEntities scan plus a set of
     * config reads. On a busy server that was a measurable chunk of the tick budget.
     */
    private void startGlowTask() {
        long interval = Math.max(1L, main.getEnchantmentsConfig().getLong("Enchantments.FEARSIGHT.Glow-Refresh-Ticks", 10L));
        Bukkit.getScheduler().runTaskTimer(main, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                updateGlow(online);
            }
        }, interval, interval);
    }

    private void updateGlow(Player p) {
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
                ChatColor aggressiveMobColor;
                try {
                    // Fall back to sensible defaults if the config key is missing (e.g. an older config file).
                    hostileMobColor = ChatColor.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FEARSIGHT.Glow-Colours.Hostile", "RED").toUpperCase());
                    passiveMobColor = ChatColor.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FEARSIGHT.Glow-Colours.Passive", "GREEN").toUpperCase());
                    normalMobColor = ChatColor.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FEARSIGHT.Glow-Colours.Player", "WHITE").toUpperCase());
                    aggressiveMobColor = ChatColor.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FEARSIGHT.Glow-Colours.Aggressive", "RED").toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return;
                }

                boolean aggressiveGlowEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.FEARSIGHT.Aggressive-Glow-Enabled", true);
                boolean aggressiveOnlyTargetingYou = main.getEnchantmentsConfig().getBoolean("Enchantments.FEARSIGHT.Aggressive-Glow-Only-Targeting-You", false);

                if (enchant.name.contains("FEARSIGHT"))
                {
                    //PUT ENCHANT LOGIC HERE
                    List<Entity> nearbyEntities = p.getNearbyEntities(enchantRadius, enchantRadius, enchantRadius);

                    // Remove glow from entities that were previously glowing but are no longer nearby
                    Map<Entity, ChatColor> previousEntities = playerEntityMap.get(p.getUniqueId());
                    if (previousEntities != null) {
                        for (Entity prevEntity : previousEntities.keySet()) {
                            if (!nearbyEntities.contains(prevEntity)) {
                                try {
                                    main.glowingEntities.unsetGlowing(prevEntity, p);
                                } catch (ReflectiveOperationException ex) {
                                    // Entity may have been removed, ignore
                                }
                            }
                        }
                    }

                    Map<Entity, ChatColor> currentEntities = new HashMap<>();

                    for (Entity entity : nearbyEntities) {
                        ChatColor glowColor;

                        // Match on the marker interfaces rather than concrete types, so mobs that
                        // don't sit under Monster/Animals (Shulker, Magma Cube, Snow Golem, Allay...)
                        // are still covered, as are any mobs added in future updates.
                        if (entity instanceof Enemy) {
                            glowColor = hostileMobColor;
                        } else if (entity instanceof Player || entity instanceof AbstractVillager || entity instanceof Golem) {
                            glowColor = normalMobColor;
                        } else if (entity instanceof Mob) {
                            glowColor = passiveMobColor;
                        } else {
                            // Not a mob (dropped item, armour stand, boat...) - never glows.
                            continue;
                        }

                        // Neutral mobs that have picked a fight get the aggressive colour instead.
                        // Always-hostile mobs are skipped as they already glow the hostile colour.
                        if (aggressiveGlowEnabled && !(entity instanceof Enemy) && isAggressive(entity, p, aggressiveOnlyTargetingYou)) {
                            glowColor = aggressiveMobColor;
                        }

                        currentEntities.put(entity, glowColor);

                        // Skip the packet if this entity is already glowing this colour for this player.
                        if (previousEntities != null && glowColor == previousEntities.get(entity)) continue;

                        try {
                            main.glowingEntities.setGlowing(entity, p, glowColor);
                        } catch (ReflectiveOperationException ex) {
                            // Logged rather than rethrown: this runs from a repeating task, and an
                            // exception escaping it would cancel the task and kill the enchant.
                            main.getLogger().warning("Fearsight could not apply glow: " + ex.getMessage());
                        }
                    }

                    playerEntityMap.put(p.getUniqueId(), currentEntities);
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

    /**
     * A neutral mob counts as aggressive once it has picked a target. Using Mob#getTarget covers
     * every neutral mob uniformly (wolves, endermen, iron golems, bees, polar bears, llamas...),
     * unlike the per-type isAngry() methods which only exist on a handful of them.
     */
    private boolean isAggressive(Entity entity, Player player, boolean onlyTargetingYou) {
        if (!(entity instanceof Mob mob)) return false;

        LivingEntity target = mob.getTarget();
        if (target == null) return false;

        return !onlyTargetingYou || target.equals(player);
    }

    private void removeGlowForPlayer(Player player) {
        Map<Entity, ChatColor> entities = playerEntityMap.remove(player.getUniqueId());
        if (entities == null) return;
        for (Entity entity : entities.keySet()) {
            try {
                main.glowingEntities.unsetGlowing(entity, player);
            } catch (ReflectiveOperationException ex) {
                main.getLogger().warning("Fearsight could not clear glow: " + ex.getMessage());
            }
        }
    }
}
