package com.atlasplugins.atlasenchants.GUIs.FearSight;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class FearsightShop implements Listener {

    private Main main;
    public FearsightShop (Main main) { this.main = main;}

    public Inventory FearSightGUI;
    public void build (Player p) {
        FearSightGUI = Bukkit.createInventory(null, InventoryType.CHEST, Main.color("&c&lFearSight Enchantments"));

        ItemStack FearSight1 = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta FearSight1Meta = FearSight1.getItemMeta();
        FearSight1Meta.setDisplayName(Main.color(main.getConfig().getString("Shop.FearSightShop.FearSight1")));
        List<String> FearSight1Lore = new ArrayList();
        for (String FearSight1Loree : main.getConfig().getStringList("Shop.FearSightShop.FearSight1Lore")) {
            FearSight1Lore.add(Main.color(FearSight1Loree));
        }
        FearSight1Meta.setLore(FearSight1Lore);
        FearSight1.setItemMeta(FearSight1Meta);

        ItemStack FearSight2 = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta FearSight2Meta = FearSight2.getItemMeta();
        FearSight2Meta.setDisplayName(Main.color(main.getConfig().getString("Shop.FearSightShop.FearSight2")));
        List<String> FearSight2Lore = new ArrayList();
        for (String FearSight2Loree : main.getConfig().getStringList("Shop.FearSightShop.FearSight2Lore")) {
            FearSight2Lore.add(Main.color(FearSight2Loree));
        }
        FearSight2Meta.setLore(FearSight2Lore);
        FearSight2.setItemMeta(FearSight2Meta);

        ItemStack FearSight3 = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta FearSight3Meta = FearSight3.getItemMeta();
        FearSight3Meta.setDisplayName(Main.color(main.getConfig().getString("Shop.FearSightShop.FearSight3")));
        List<String> FearSight3Lore = new ArrayList();
        for (String FearSight3Loree : main.getConfig().getStringList("Shop.FearSightShop.FearSight3Lore")) {
            FearSight3Lore.add(Main.color(FearSight3Loree));
        }
        FearSight3Meta.setLore(FearSight3Lore);
        FearSight3.setItemMeta(FearSight3Meta);

        ItemStack Filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta FillerMeta = Filler.getItemMeta();
        FillerMeta.setDisplayName(Main.color(main.getConfig().getString("Shop.FillerName")));
        List<String> FillerLore = new ArrayList();
        for (String FillerLoree : main.getConfig().getStringList("Shop.FillerLore")) {
            FillerLore.add(Main.color(FillerLoree));
        }
        FillerMeta.setLore(FillerLore);
        Filler.setItemMeta(FillerMeta);

        FearSightGUI.setItem(0, Filler);
        FearSightGUI.setItem(1, Filler);
        FearSightGUI.setItem(2, Filler);
        FearSightGUI.setItem(3, Filler);
        FearSightGUI.setItem(4, Filler);
        FearSightGUI.setItem(5, Filler);
        FearSightGUI.setItem(6, Filler);
        FearSightGUI.setItem(7, Filler);
        FearSightGUI.setItem(8, Filler);
        FearSightGUI.setItem(9, Filler);
        FearSightGUI.setItem(10, Filler);
        FearSightGUI.setItem(11, FearSight1);
        FearSightGUI.setItem(12, Filler);
        FearSightGUI.setItem(13, FearSight2);
        FearSightGUI.setItem(14, Filler);
        FearSightGUI.setItem(15, FearSight3);
        FearSightGUI.setItem(16, Filler);
        FearSightGUI.setItem(17, Filler);
        FearSightGUI.setItem(18, Filler);
        FearSightGUI.setItem(19, Filler);
        FearSightGUI.setItem(20, Filler);
        FearSightGUI.setItem(21, Filler);
        FearSightGUI.setItem(22, Filler);
        FearSightGUI.setItem(23, Filler);
        FearSightGUI.setItem(24, Filler);
        FearSightGUI.setItem(25, Filler);
        FearSightGUI.setItem(26, Filler);
    }

    public void show(Player p) {
        Main.instance.FearSightInventory.put(p, FearSightGUI);
        p.openInventory(FearSightGUI);
    }

}
