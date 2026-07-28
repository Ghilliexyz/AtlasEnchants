package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.armorevents.ArmorEquipEvent;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class Asclepius implements Listener {

    private Main main;

    public Asclepius(Main main) {
        this.main = main;

        // A /reload builds a fresh instance while players are still online wearing their gear, so
        // re-derive the state for everyone rather than leaving a boost nothing can remove.
        Bukkit.getScheduler().runTask(main, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                PotionEffect existing = online.getPotionEffect(PotionEffectType.HEALTH_BOOST);
                // An infinite duration is this enchant's signature - potions are always finite -
                // so adopting only those leaves a player's potion effect alone.
                if (existing != null && existing.getDuration() == PotionEffect.INFINITE_DURATION) {
                    boosted.add(online.getUniqueId());
                }
                refresh(online);
            }
        });
    }

    /**
     * Players whose Health Boost was applied by this enchant.
     *
     * <p>Without this the enchant removed HEALTH_BOOST unconditionally, which also wiped the effect
     * when it came from a potion or another plugin - drink a Health Boost potion, swap a boot, and
     * it was gone.
     */
    private final Set<UUID> boosted = new HashSet<>();

    public boolean hasArmor (Player p) {
        // Get the player's chestplate item
        ItemStack armor = p.getInventory().getChestplate();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.ASCLEPIUS.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable chestplate
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        // The chestplate contents are only updated after the equip event returns, so the refresh is
        // deferred a tick - reading them here would see the state from before the swap.
        Bukkit.getScheduler().runTask(main, () -> refresh(event.getPlayer()));
    }

    /** Re-apply on join: potion effects do not survive a relog, but the chestplate does. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        boosted.remove(event.getPlayer().getUniqueId());
        refresh(event.getPlayer());
    }

    /** Re-apply on respawn: death clears every potion effect, including this one. */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        boosted.remove(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTask(main, () -> refresh(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        boosted.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Bring the player's Health Boost in line with the chestplate they are currently wearing.
     * Applies, updates or removes as needed, and only ever removes a boost this enchant applied.
     */
    private void refresh(Player p) {
        if (!p.isOnline()) return;

        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.ASCLEPIUS.Enchantment-Enabled");

        int healthBoostLevel = 0;
        if (isEnchantmentEnabled && hasArmor(p)) {
            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getChestplate())) {
                if (enchant.name.contains("ASCLEPIUS")) {
                    healthBoostLevel = main.getEnchantmentsConfig().getInt("Enchantments.ASCLEPIUS.Asclepius-HealthBoost-" + enchant.level);
                    break;
                }
            }
        }

        if (healthBoostLevel > 0) {
            setPlayerMaxHealth(p, healthBoostLevel);
        } else {
            removePlayerMaxHealth(p);
        }
    }

    private void setPlayerMaxHealth(Player p, int level) {
        PotionEffect potionType = new PotionEffect(PotionEffectType.HEALTH_BOOST, PotionEffect.INFINITE_DURATION, level - 1, false, false, true);
        p.addPotionEffect(potionType);
        boosted.add(p.getUniqueId());
    }

    private void removePlayerMaxHealth(Player p) {
        // Only strip the effect if this enchant is the one that granted it.
        if (!boosted.remove(p.getUniqueId())) return;
        p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
    }
}
