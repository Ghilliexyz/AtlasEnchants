package com.atlasplugins.atlasenchants.Enchants;

import com.atlasplugins.atlasenchants.Main;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.xezard.glow.data.glow.Glow;

import java.util.Arrays;
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

        Player player = e.getPlayer();
        List<Entity> listE = player.getNearbyEntities(50, 50, 50);

        for (Entity entity: listE) {

            Glow glowRed = Glow.builder()
                    .color(ChatColor.DARK_RED)
                    .name("redglow")
                    .build();

            if (entity instanceof org.bukkit.entity.Monster ||
                    entity instanceof org.bukkit.entity.Flying ||
                    entity instanceof org.bukkit.entity.Slime ||
                    entity instanceof org.bukkit.entity.Boss) {

                glowRed.addHolders(entity);
                glowRed.display(player);
            }else

            if (entity instanceof org.bukkit.entity.Animals ||
                    entity instanceof org.bukkit.entity.Ambient ||
                    entity instanceof org.bukkit.entity.WaterMob) {

//                    GlowAPI.setGlowing(entity, GlowAPI.Color.DARK_GREEN, player);
            }else

            if (entity instanceof Player ||
                    entity instanceof org.bukkit.entity.Villager ||
                    entity instanceof org.bukkit.entity.WanderingTrader ||
                    entity instanceof org.bukkit.entity.IronGolem ) {

//                    GlowAPI.setGlowing(entity, GlowAPI.Color.WHITE, player);
            }

//            if (entity.getLocation().distance(player.getLocation()) >= main.getConfig().getInt("Fearsight.radius-of-glowing-" + level)) {
//                glowRed.removeHolders(entity);
//            }
        }

        if (main.hasHelmet.containsKey(player)) {
            if (main.ColorTask.containsKey(player)) {
                Bukkit.getScheduler().cancelTask(((BukkitTask)main.ColorTask.get(player)).getTaskId());
                main.ColorTask.remove(player);
            }

            ItemStack helmet = player.getInventory().getHelmet();
            ItemMeta helmetMeta = helmet.getItemMeta();
            String EnchantName = Main.color("&cFearsight I");
            int level = 0;

            for (String a: helmetMeta.getLore()){
                if (a.equals(EnchantName)) {
                    level = 1;
                    listE = player.getNearbyEntities(
                            main.getConfig().getInt("Fearsight.radius-of-glowing-1"),
                            main.getConfig().getInt("Fearsight.radius-of-glowing-1"),
                            main.getConfig().getInt("Fearsight.radius-of-glowing-1"));
                }
                if (a.equals(String.valueOf(EnchantName) + "I")) {
                    level = 2;
                    listE = player.getNearbyEntities(
                            main.getConfig().getInt("Fearsight.radius-of-glowing-2"),
                            main.getConfig().getInt("Fearsight.radius-of-glowing-2"),
                            main.getConfig().getInt("Fearsight.radius-of-glowing-2"));
                }
                if (a.equals(String.valueOf(EnchantName) + "II")) {
                    level = 3;
                    listE = player.getNearbyEntities(
                            main.getConfig().getInt("Fearsight.radius-of-glowing-3"),
                            main.getConfig().getInt("Fearsight.radius-of-glowing-3"),
                            main.getConfig().getInt("Fearsight.radius-of-glowing-3"));
                }
            }

//            for (Entity entity: listE) {
//
//                if (entity instanceof org.bukkit.entity.Monster ||
//                        entity instanceof org.bukkit.entity.Flying ||
//                        entity instanceof org.bukkit.entity.Slime ||
//                        entity instanceof org.bukkit.entity.Boss) {
//
//                }else
//
//                if (entity instanceof org.bukkit.entity.Animals ||
//                        entity instanceof org.bukkit.entity.Ambient ||
//                        entity instanceof org.bukkit.entity.WaterMob) {
//
////                    GlowAPI.setGlowing(entity, GlowAPI.Color.DARK_GREEN, player);
//                }else
//
//                if (entity instanceof Player ||
//                        entity instanceof org.bukkit.entity.Villager ||
//                        entity instanceof org.bukkit.entity.WanderingTrader ||
//                        entity instanceof org.bukkit.entity.IronGolem ) {
//
////                    GlowAPI.setGlowing(entity, GlowAPI.Color.WHITE, player);
//                }
//
//                if (entity.getLocation().distance(player.getLocation()) >= main.getConfig().getInt("Fearsight.radius-of-glowing-" + level)) {
//                    glowRed.removeHolders(entity);
//                }
//            }

            if (main.playerEntities.get(player) != listE) {
                main.playerEntities.put(player, listE);
            }else
            if (!main.ColorTask.containsKey(player)) {
                main.ColorTask.put(player, (new BukkitRunnable() {
                    public void run() {
                        for (Entity entity : player.getNearbyEntities(55.0D, 55.0D, 55.0D)) {
//                            glowRed.removeHolders(entity);
                        }
                    }
                }).runTaskTimer(main, 0L, 15L));
            }

        }
    }

//    @EventHandler
//    public void PlayerDeathEvent(PlayerDeathEvent e) {
//        Player player = e.getEntity();
//        for (Entity entity : player.getNearbyEntities(50.0D, 50.0D, 50.0D)) {
//            if (GlowAPI.isGlowing(entity, player)) {
//                GlowAPI.setGlowing(entity, false, player);
//            }
//        }
//    }

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
        if (main.ColorTask.containsKey(player)) {
            Bukkit.getScheduler().cancelTask(((BukkitTask)main.ColorTask.get(player)).getTaskId());
            main.ColorTask.remove(player);
        }
    }

    @EventHandler
    public void PlayerEquipEvent(ArmorEquipEvent e) {
        Player player = e.getPlayer();
        if (e.getNewArmorPiece() != null && e.getNewArmorPiece().getType() != Material.AIR) {
            if (e.getNewArmorPiece().getItemMeta().hasLore())
                if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().getType() != Material.AIR && !isHelmet(e.getNewArmorPiece().getType()) && !hasCustomEnchant(e.getNewArmorPiece())) {
                    e.setCancelled(true);
                } else if (!main.hasHelmet.containsKey(player)) {
                    main.hasHelmet.put(player, Boolean.valueOf(true));
                }
        } else
        if (e.getOldArmorPiece() != null && e.getOldArmorPiece().getType() != Material.AIR && e.getOldArmorPiece().getItemMeta().hasLore() && hasCustomEnchant(e.getOldArmorPiece())) {

            if (main.hasHelmet.containsKey(player)) {
                main.hasHelmet.remove(player);
            }
//            if (main.playerEntities.containsKey(player)) {
//                for (Entity entity : main.playerEntities.get(player)) {
//                    if (GlowAPI.isGlowing(entity, player))
//                        GlowAPI.setGlowing(entity, false, player);
//                }
//            }
            if (main.playerEntities.containsKey(player)) {
                main.playerEntities.remove(player);
            }
        }
    }

    @EventHandler
    public void DragEnchantOnToItem(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) {
            return;
        }

        ItemStack slot = e.getCurrentItem();
        ItemMeta slotMeta = slot.getItemMeta();
        ItemStack cursore = e.getCursor();
        Player player = (Player) e.getWhoClicked();
        String nameCursor = Main.color("&cFearsight I");
        String[] parts = nameCursor.split(" ");
        String name = parts[0];
        if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR && slot != null && cursore != null && isHelmet(slot.getType()) &&
                cursore.getItemMeta().getDisplayName().contains(name) && cursore.getItemMeta().hasLore() && e.getSlotType() != InventoryType.SlotType.ARMOR)
            if (cursore.getAmount() == 1) {
                if (isHelmet(slot.getType())) {
                    if (slotMeta.hasLore()) {
                        List<String> l = slotMeta.getLore();
                        for (String row : l) {
                            if (row.equals(nameCursor)) {
                                l.set(l.indexOf(row), cursore.getItemMeta().getDisplayName());
                                continue;
                            }
                            if (row.equals(String.valueOf(nameCursor) + "I")) {
                                l.set(l.indexOf(row), cursore.getItemMeta().getDisplayName());
                                continue;
                            }
                            if (row.equals(String.valueOf(nameCursor) + "II")) {
                                player.sendMessage(Main.color(main.getConfig().getString("Messages.max-enchant-message")));
                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                                e.setCancelled(true);
                            }
                        }
                        slotMeta.setLore(l);
                    } else {
                        slotMeta.setLore(Arrays.asList(new String[] { cursore.getItemMeta().getDisplayName() }));
                    }
                    if (!e.isCancelled())
                        player.setItemOnCursor(new ItemStack(Material.AIR));
                    slot.setItemMeta(slotMeta);
                    e.setCurrentItem(slot);
                }
                e.setCancelled(true);
            } else {
                player.sendMessage(Main.color(main.getConfig().getString("Messages.enchant-error-message")));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                e.setCancelled(true);
            }
    }

    @EventHandler
    public void playercloseGUI(InventoryCloseEvent event) {
        Player player = (Player)event.getPlayer();
        if (player.isOnline() && event.getView().getTitle().equalsIgnoreCase(Main.color(main.getConfig().getString(Main.color("BlackSmith.InventoryName")))))
            if (event.getInventory().getItem(11) != null || event.getInventory().getItem(15) != null) {
                if (player.getInventory().firstEmpty() != -1) {
                    if (getEmptySlots(player) >= 2) {
                        if (event.getInventory().getItem(11) == null) {
                            player.getInventory().addItem(new ItemStack[] { event.getInventory().getItem(15) });
                            event.getInventory().clear();
                        } else if (event.getInventory().getItem(15) == null) {
                            player.getInventory().addItem(new ItemStack[] { event.getInventory().getItem(11) });
                            event.getInventory().clear();
                        } else {
                            player.getInventory().addItem(new ItemStack[] { event.getInventory().getItem(11) });
                            player.getInventory().addItem(new ItemStack[] { event.getInventory().getItem(15) });
                            event.getInventory().clear();
                        }
                    } else {
                        player.getWorld().dropItem(player.getLocation(), event.getInventory().getItem(11));
                        player.getWorld().dropItem(player.getLocation(), event.getInventory().getItem(15));
                        player.sendMessage(Main.color(main.getConfig().getString("Messages.inventory-full-message-onclose")));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                    }
                } else {
                    player.getWorld().dropItem(player.getLocation(), event.getInventory().getItem(11));
                    player.getWorld().dropItem(player.getLocation(), event.getInventory().getItem(15));
                    player.sendMessage(Main.color(main.getConfig().getString("Messages.inventory-full-message-onclose")));
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                }
            } else {
                return;
            }
    }

}
