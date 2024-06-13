package me.helix.atlasenchants.GUIs;

import me.helix.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopGUI {

    private Main main;
    public ShopGUI (Main main) {
        this.main = main;
    }

    public Inventory ShopGUI;
    public void build (Player p) {
        ShopGUI = Bukkit.createInventory(null, InventoryType.CHEST, Main.color(main.getConfig().getString("Shop.InventoryName")));

        ItemStack Godly = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta GodlyMeta = Godly.getItemMeta();
        GodlyMeta.setDisplayName(Main.color("&c&lGODLY"));
        Godly.setItemMeta(GodlyMeta);

        ItemStack Mythic = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta MythicMeta = Mythic.getItemMeta();
        MythicMeta.setDisplayName(Main.color("&e&lMythic"));
        Mythic.setItemMeta(MythicMeta);

        ItemStack Rare = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta RareeMeta = Rare.getItemMeta();
        RareeMeta.setDisplayName(Main.color("&5&lRare"));
        Rare.setItemMeta(RareeMeta);

        ItemStack Filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta FillerMeta = Filler.getItemMeta();
        FillerMeta.setDisplayName(Main.color("&c&lF&6&lU&e&lC&2&lK &b&lO&9&lF&5&lF&f&l!!!"));
        Filler.setItemMeta(FillerMeta);

        ShopGUI.setItem(0, Filler);
        ShopGUI.setItem(1, Filler);
        ShopGUI.setItem(2, Filler);
        ShopGUI.setItem(3, Filler);
        ShopGUI.setItem(4, Filler);
        ShopGUI.setItem(5, Filler);
        ShopGUI.setItem(6, Filler);
        ShopGUI.setItem(7, Filler);
        ShopGUI.setItem(8, Filler);
        ShopGUI.setItem(9, Filler);
        ShopGUI.setItem(10, Godly);
        ShopGUI.setItem(11, Filler);
        ShopGUI.setItem(12, Filler);
        ShopGUI.setItem(13, Mythic);
        ShopGUI.setItem(14, Filler);
        ShopGUI.setItem(15, Filler);
        ShopGUI.setItem(16, Rare);
        ShopGUI.setItem(17, Filler);
        ShopGUI.setItem(18, Filler);
        ShopGUI.setItem(19, Filler);
        ShopGUI.setItem(20, Filler);
        ShopGUI.setItem(21, Filler);
        ShopGUI.setItem(22, Filler);
        ShopGUI.setItem(23, Filler);
        ShopGUI.setItem(24, Filler);
        ShopGUI.setItem(25, Filler);
        ShopGUI.setItem(26, Filler);
    }

    public void show(Player p) {
        Main.instance.ShopInventory.put(p, ShopGUI);
        p.openInventory(ShopGUI);
    }

}
