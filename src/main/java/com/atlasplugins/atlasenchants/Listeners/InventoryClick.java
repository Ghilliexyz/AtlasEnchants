package com.atlasplugins.atlasenchants.Listeners;

import com.atlasplugins.atlasenchants.GUIs.FearSight.FearsightShop;
import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class InventoryClick implements Listener {

    private Main main;
    public InventoryClick (Main main) {
        this.main = main;
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

    @EventHandler
    public void ShopGUIClick (InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        //Shop GUI
        if (e.getView().getTitle().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.InventoryName")))) {

            if (e.getCurrentItem() == null) {
                return;
            }

//            if ((e.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE || e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("&c&lGODLY"))) {
//                GodlyEnchants godly = new GodlyEnchants(main);
//                godly.build(player);
//                godly.show(player);
//                player.updateInventory();
//                e.setCancelled(true);
//            }

            if (e.getCurrentItem().getType() == Material.YELLOW_STAINED_GLASS_PANE || e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("&e&lMythic")) {
                e.setCancelled(true);
            }

            if (e.getCurrentItem().getType() == Material.PURPLE_STAINED_GLASS_PANE || e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("&5&lRare")) {
                e.setCancelled(true);
            }

            if (e.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) {
                e.setCancelled(true);
            }

        }

        //Godly Enchant GUI
        if (e.getView().getTitle().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.GodlyEnchantsList.GodlyEnchantsName")))) {

            if (e.getCurrentItem() == null) {
                return;
            }

            if (e.getCurrentItem().getType() == Material.HEART_OF_THE_SEA || e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.GodlyEnchantsList.FearSightName")))) {
                e.setCancelled(true);
            }

            if (e.getCurrentItem().getType() == Material.HEART_OF_THE_SEA || e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.FillerName")))) {
                e.setCancelled(true);
            }

            if (e.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) {
                e.setCancelled(true);
            }

        }

        //FearSight GUI
        if (e.getView().getTitle().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.FearSightShop.FearSightShopName")))) {

            if (e.getCurrentItem() == null) {
                return;
            }

            if (e.getCurrentItem().getType() == Material.HEART_OF_THE_SEA || e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.GodlyEnchantsList.FearSightName")))) {
                FearsightShop fear = new FearsightShop(main);
                fear.build(player);
                fear.show(player);
                player.updateInventory();
                e.setCancelled(true);
            }

            if (e.getCurrentItem().getType() == Material.HEART_OF_THE_SEA || e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.FearSightShop.FearSight2")))) {
                e.setCancelled(true);
            }

            if (e.getCurrentItem().getType() == Material.HEART_OF_THE_SEA || e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.FearSightShop.FearSight3")))) {
                e.setCancelled(true);
            }

            if (e.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) {
                e.setCancelled(true);
            }

        }

        //FearSight Buying GUI
        if (e.getView().getTitle().equalsIgnoreCase(Main.color("&c&lFearSight Enchantments"))) {

            if (e.getCurrentItem() == null) {
                return;
            }

            int nnDiamond = 0;
            byte bb;
            int ii;
            ItemStack[] arrayyOfItemStack;
            for (ii = (arrayyOfItemStack = player.getInventory().getContents()).length, bb = 0; bb < ii; ) {
                ItemStack is = arrayyOfItemStack[bb];
                if (is != null &&
                        is.getType() == Material.DIAMOND)
                    nnDiamond += is.getAmount();
                bb++;
            }

            if (e.getCurrentItem().getType() == Material.HEART_OF_THE_SEA && e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.FearSightShop.FearSight1")))) {
                if (nnDiamond >= 21) {
                    ItemStack FearSight = new ItemStack(Material.HEART_OF_THE_SEA);
                    ItemMeta FearSightMeta = FearSight.getItemMeta();
                    FearSightMeta.setDisplayName(Main.color(Main.color("&cFearsight I")));
                    FearSightMeta.setLore(Collections.singletonList(Main.color(main.getConfig().getString("Fearsight.lore-for-Fearsight"))));
                    FearSight.setItemMeta(FearSightMeta);

                    player.getInventory().removeItem(new ItemStack[]{new ItemStack(Material.DIAMOND, 21)});
                    player.getInventory().addItem(new ItemStack[]{FearSight});
                    e.setCancelled(true);
                    return;
                } else{
                    player.sendMessage(Main.color(main.getConfig().getString("Messages.not-enought-diamonds")));
                    e.setCancelled(true);
                }
            }

            if (e.getCurrentItem().getType() == Material.HEART_OF_THE_SEA && e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.FearSightShop.FearSight2")))) {
                if (nnDiamond >= 32) {
                    ItemStack FearSight = new ItemStack(Material.HEART_OF_THE_SEA);
                    ItemMeta FearSightMeta = FearSight.getItemMeta();
                    FearSightMeta.setDisplayName(Main.color(Main.color("&cFearsight II")));
                    FearSightMeta.setLore(Collections.singletonList(Main.color(main.getConfig().getString("Fearsight.lore-for-Fearsight"))));
                    FearSight.setItemMeta(FearSightMeta);

                    player.getInventory().removeItem(new ItemStack[]{new ItemStack(Material.DIAMOND, 32)});
                    player.getInventory().addItem(new ItemStack[]{FearSight});
                    e.setCancelled(true);
                    return;
                } else{
                    player.sendMessage(Main.color(main.getConfig().getString("Messages.not-enought-diamonds")));
                    e.setCancelled(true);
                }
            }

            if (e.getCurrentItem().getType() == Material.HEART_OF_THE_SEA && e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Main.color(main.getConfig().getString("Shop.FearSightShop.FearSight3")))) {
                if (nnDiamond >= 64) {
                    ItemStack FearSight = new ItemStack(Material.HEART_OF_THE_SEA);
                    ItemMeta FearSightMeta = FearSight.getItemMeta();
                    FearSightMeta.setDisplayName(Main.color(Main.color("&cFearsight III")));
                    FearSightMeta.setLore(Collections.singletonList(Main.color(main.getConfig().getString("Fearsight.lore-for-Fearsight"))));
                    FearSight.setItemMeta(FearSightMeta);

                    player.getInventory().removeItem(new ItemStack[]{new ItemStack(Material.DIAMOND, 64)});
                    player.getInventory().addItem(new ItemStack[]{FearSight});
                    e.setCancelled(true);
                    return;
                } else{
                    player.sendMessage(Main.color(main.getConfig().getString("Messages.not-enought-diamonds")));
                    e.setCancelled(true);
                }
            }

            if (e.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) {
                e.setCancelled(true);
            }

        }

        //BlackSmith GUI
        if (e.getView().getTitle().equalsIgnoreCase(Main.color(main.getConfig().getString("BlackSmith.InventoryName")))) {
            if (e.getCurrentItem() == null) {
                return;
            }
            if (e.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) {
                e.setCancelled(true);
            } else
            if (e.getCurrentItem().getType() == Material.LIME_STAINED_GLASS_PANE && e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(Main.color("&a&lUpgrade!"))) {

                if (e.getInventory().getItem(11) == null || e.getInventory().getItem(15) == null) {
                    e.setCancelled(true);
                } else {
                    String nameCustomItem = Main.color("&cFearsight I");

                    if (e.getInventory().getItem(11).getType() == Material.getMaterial( main.getConfig().getString("Fearsight.customItem"))
                            && e.getInventory().getItem(15).getType() == Material.getMaterial( main.getConfig().getString("Fearsight.customItem"))
                            && e.getInventory().getItem(11).getItemMeta().getDisplayName().equals(e.getInventory().getItem(15).getItemMeta().getDisplayName())) {

                        if (e.getInventory().getItem(11).getAmount() == 1 && e.getInventory().getItem(15).getAmount() == 1) {
                            if (player.getInventory().firstEmpty() != -1) {
                                if (getEmptySlots(player) >= 2) {
                                    ItemStack slot1 = e.getInventory().getItem(11);
                                    ItemMeta slotMeta1 = slot1.getItemMeta();
                                    ItemStack slot2 = e.getInventory().getItem(15);
                                    ItemMeta slotMeta2 = slot2.getItemMeta();
                                    ItemStack vuoto = new ItemStack(Material.AIR);
                                    if (slotMeta1.hasLore() && slotMeta2.hasLore()) {
                                        int nDiamond = 0;
                                        byte b;
                                        int i;
                                        ItemStack[] arrayOfItemStack;
                                        for (i = (arrayOfItemStack = player.getInventory().getContents()).length, b = 0; b < i; ) {
                                            ItemStack is = arrayOfItemStack[b];
                                            if (is != null &&
                                                    is.getType() == Material.DIAMOND)
                                                nDiamond += is.getAmount();
                                            b++;
                                        }

                                        if (slotMeta1.getDisplayName().equals(Main.color("&cFearsight I")) && slotMeta2.getDisplayName().equals(Main.color("&cFearsight I"))) {
                                            if (nDiamond >= main.getConfig().getInt("Fearsight.amount-of-diamonds-to-level-2")) {
                                                slotMeta1.setDisplayName(String.valueOf(slotMeta1.getDisplayName()) + "I");
                                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F);
                                                player.getInventory().removeItem(new ItemStack[] { new ItemStack(Material.DIAMOND, main.getConfig().getInt("Fearsight.amount-of-diamonds-to-level-2")) });
                                            } else {
                                                player.sendMessage(Main.color(main.getConfig().getString("Messages.no-diamond")));
                                                player.sendMessage(Main.color("need " + main.getConfig().getInt("Fearsight.amount-of-diamonds-to-level-2") + " diamonds."));
                                                player.getInventory().addItem(new ItemStack[] { slot2 });
                                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                                                e.setCancelled(true);
                                            }
                                        }

                                        if (slotMeta1.getDisplayName().equals(Main.color("&cFearsight II")) && slotMeta2.getDisplayName().equals(Main.color("&cFearsight II"))) {
                                            if (nDiamond >= main.getConfig().getInt("Fearsight.amount-of-diamonds-to-level-3")) {
                                                slotMeta1.setDisplayName(String.valueOf(slotMeta1.getDisplayName()) + "I");
                                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0F, 1.0F);
                                                player.getInventory().removeItem(new ItemStack[]{new ItemStack(Material.DIAMOND, main.getConfig().getInt("Fearsight.amount-of-diamonds-to-level-3"))});
                                            } else {
                                                player.sendMessage(Main.color(main.getConfig().getString("Messages.no-diamond")));
                                                player.sendMessage(Main.color("need " + main.getConfig().getInt("Fearsight.amount-of-diamonds-to-level-3") + " diamonds."));
                                                player.getInventory().addItem(new ItemStack[]{slot2});
                                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                                                e.setCancelled(true);
                                            }
                                        }

                                        if (slotMeta1.getDisplayName().equals(Main.color("&cFearsight III")) && slotMeta2.getDisplayName().equals(Main.color("&cFearsight III"))) {
                                            player.sendMessage(Main.color(main.getConfig().getString("Messages.max-enchant-message")));
                                        }

                                        e.getInventory().setItem(11, vuoto);
                                        e.getInventory().setItem(15, vuoto);
                                        slot1.setItemMeta(slotMeta1);
                                        player.getInventory().addItem(new ItemStack[] { slot1 });
                                        e.setCancelled(true);
                                    }
                                } else {
                                    player.sendMessage(Main.color(main.getConfig().getString("Messages.inventory-full-message-onopen")));
                                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                                    e.setCancelled(true);
                                }
                            } else {
                                player.sendMessage(Main.color(main.getConfig().getString("Messages.inventory-full-message-onopen")));
                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                                e.setCancelled(true);
                            }
                        } else {
                            player.sendMessage(Main.color(main.getConfig().getString("Messages.enchant-error-message")));
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                            e.setCancelled(true);
                        }
                    } else {
                        e.setCancelled(true);
                    }
                }
            }
        }

    }

}
