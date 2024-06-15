package com.atlasplugins.atlasenchants.Enchants;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class Fearsight implements Listener {

    private Main main;
    public Fearsight (Main main) {
        this.main = main;
    }

    public boolean isHelmet(Material type) {
        if ((type == Material.LEATHER_HELMET) ||
                (type == Material.IRON_HELMET) ||
                (type == Material.CHAINMAIL_HELMET) ||
                (type == Material.GOLDEN_HELMET) ||
                (type == Material.DIAMOND_HELMET) ||
                (type == Material.NETHERITE_HELMET) ||
                (type == Material.TURTLE_HELMET)) {
            return true;
        }
        return false;
    }

    public int getEmptySlots(Player p) {
        PlayerInventory playerInventory = p.getInventory();
        ItemStack[] cont = playerInventory.getContents();
        int i = 0;
        byte b;
        int j;
        ItemStack[] arrayOfItemStack1;
        for (j = (arrayOfItemStack1 = cont).length, b = 0; b < j; ) {
            ItemStack item = arrayOfItemStack1[b];
            if (item != null && item.getType() != Material.AIR)
                i++;
            b++;
        }
        return 36 - i;
    }

    public boolean hasCustomEnchant(ItemStack helmet) {
        ItemMeta helmetMeta = helmet.getItemMeta();
        if (helmetMeta.hasLore()) {
            for (String lore: helmetMeta.getLore()) {
                if ((lore.equals(Main.color("&cFearsight I"))) || (lore.equals(Main.color("&cFearsight II"))) || (lore.equals(Main.color("&cFearsight III")))) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {

        Player p = e.getPlayer();
        List<Entity> listE = p.getNearbyEntities(15, 15, 15);

        String hostileMobColor = main.getConfig().getString("Enchantments.FEARSIGHT.Hostile-GLOW-Colour").toUpperCase();
        String passiveMobColor = main.getConfig().getString("Enchantments.FEARSIGHT.Passive-GLOW-Colour").toUpperCase();
        String normalMobColor = main.getConfig().getString("Enchantments.FEARSIGHT.Player-Villager").toUpperCase();

        for (Entity entity: listE)
        {
            if (entity instanceof org.bukkit.entity.Monster ||
                    entity instanceof org.bukkit.entity.Flying ||
                    entity instanceof org.bukkit.entity.Slime ||
                    entity instanceof org.bukkit.entity.Boss)
            {
                    try {
                        main.glowingEntities.setGlowing(entity, p, ChatColor.valueOf(hostileMobColor));
                    } catch (ReflectiveOperationException ex) {
                        throw new RuntimeException(ex);
                    }
            }
            else if (entity instanceof org.bukkit.entity.Animals ||
                    entity instanceof org.bukkit.entity.Ambient ||
                    entity instanceof org.bukkit.entity.WaterMob)
            {

                    try {
                        main.glowingEntities.setGlowing(entity, p, ChatColor.valueOf(passiveMobColor));
                    } catch (ReflectiveOperationException ex) {
                        throw new RuntimeException(ex);
                    }
            }
            else if (entity instanceof Player ||
                    entity instanceof org.bukkit.entity.Villager ||
                    entity instanceof org.bukkit.entity.WanderingTrader ||
                    entity instanceof org.bukkit.entity.IronGolem )
            {
                    try {
                        main.glowingEntities.setGlowing(entity, p, ChatColor.valueOf(normalMobColor));
                    } catch (ReflectiveOperationException ex) {
                        throw new RuntimeException(ex);
                    }
            }

//            if (entity.getLocation().distance(p.getLocation()) >= main.getConfig().getInt("Fearsight.radius-of-glowing-" + 15))
            if(entity.getLocation().distance(p.getLocation()) >= 15)
            {
                try {
                    main.glowingEntities.unsetGlowing(entity, p);

                } catch (ReflectiveOperationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (main.hasHelmet.containsKey(p)) {
            if (main.ColorTask.containsKey(p)) {
                Bukkit.getScheduler().cancelTask(((BukkitTask)main.ColorTask.get(p)).getTaskId());
                main.ColorTask.remove(p);
            }

            ItemStack helmet = p.getInventory().getHelmet();
            ItemMeta helmetMeta = helmet.getItemMeta();
            String EnchantName = Main.color("&cFearsight I");
            int level = 0;

            for (String a: helmetMeta.getLore()){
                if (a.equals(EnchantName)) {
                    level = 1;
                    listE = p.getNearbyEntities(
                            main.getConfig().getInt("Enchantments.Fearsight.radius-of-glowing-1"),
                            main.getConfig().getInt("Enchantments.Fearsight.radius-of-glowing-1"),
                            main.getConfig().getInt("Enchantments.Fearsight.radius-of-glowing-1"));
                }
                if (a.equals(String.valueOf(EnchantName) + "I")) {
                    level = 2;
                    listE = p.getNearbyEntities(
                            main.getConfig().getInt("Enchantments.Fearsight.radius-of-glowing-2"),
                            main.getConfig().getInt("Enchantments.Fearsight.radius-of-glowing-2"),
                            main.getConfig().getInt("Enchantments.Fearsight.radius-of-glowing-2"));
                }
                if (a.equals(String.valueOf(EnchantName) + "II")) {
                    level = 3;
                    listE = p.getNearbyEntities(
                            main.getConfig().getInt("Enchantments.Fearsight.radius-of-glowing-3"),
                            main.getConfig().getInt("Enchantments.Fearsight.radius-of-glowing-3"),
                            main.getConfig().getInt("Enchantments.Fearsight.radius-of-glowing-3"));
                }
            }

            if (main.playerEntities.get(p) != listE) {
                main.playerEntities.put(p, listE);
            }else
            if (!main.ColorTask.containsKey(p)) {
                main.ColorTask.put(p, (new BukkitRunnable() {
                    public void run() {
                        for (Entity entity : p.getNearbyEntities(55.0D, 55.0D, 55.0D)) {
                            try {
                                main.glowingEntities.unsetGlowing(entity, p);
                            } catch (ReflectiveOperationException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                }).runTaskTimer(main, 0L, 15L));
            }

        }
    }

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent e) throws ReflectiveOperationException {
        Player player = e.getEntity();
        for (Entity entity : player.getNearbyEntities(50.0D, 50.0D, 50.0D)) {
            try {
                main.glowingEntities.unsetGlowing(entity, player);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e){
        Player player = e.getPlayer();
        if (player.getInventory().getHelmet() != null &&
                isHelmet(player.getInventory().getHelmet().getType()) && hasCustomEnchant(player.getInventory().getHelmet()) &&
                !main.hasHelmet.containsKey(player))
            main.hasHelmet.put(player, Boolean.valueOf(true));
    }

    @EventHandler
    public void PlayerLeaveEvent(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        for (Entity entity : player.getNearbyEntities(50.0D, 50.0D, 50.0D)) {
            try {
                main.glowingEntities.unsetGlowing(entity, player);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (main.ColorTask.containsKey(player)) {
            Bukkit.getScheduler().cancelTask(((BukkitTask)main.ColorTask.get(player)).getTaskId());
            main.ColorTask.remove(player);
        }
    }

}
