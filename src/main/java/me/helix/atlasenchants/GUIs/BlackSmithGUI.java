package me.helix.atlasenchants.GUIs;

import me.helix.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlackSmithGUI {

    private Main main;
    public BlackSmithGUI(Main main) {
        this.main = main;
    }

    public Inventory BlackSmithGUI;
    public void build (Player p) {
        BlackSmithGUI = Bukkit.createInventory(null, InventoryType.CHEST, Main.color(main.getConfig().getString(Main.color("BlackSmith.InventoryName"))));

        ItemStack vetro = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta vetroMeta = vetro.getItemMeta();
        vetroMeta.setDisplayName(Main.color("&c&lF&6&lU&e&lC&2&lK &b&lO&9&lF&5&lF&f&l!!!"));
        vetro.setItemMeta(vetroMeta);

        ItemStack upgrade = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta upgradeMeta = upgrade.getItemMeta();
        upgradeMeta.setDisplayName(Main.color("&a&lUpgrade!"));
        upgrade.setItemMeta(upgradeMeta);

        BlackSmithGUI.setItem(0, vetro);
        BlackSmithGUI.setItem(1, vetro);
        BlackSmithGUI.setItem(2, vetro);
        BlackSmithGUI.setItem(3, vetro);
        BlackSmithGUI.setItem(4, vetro);
        BlackSmithGUI.setItem(5, vetro);
        BlackSmithGUI.setItem(6, vetro);
        BlackSmithGUI.setItem(7, vetro);
        BlackSmithGUI.setItem(8, vetro);
        BlackSmithGUI.setItem(9, vetro);
        BlackSmithGUI.setItem(10, vetro);
        BlackSmithGUI.setItem(12, vetro);
        BlackSmithGUI.setItem(13, upgrade);
        BlackSmithGUI.setItem(14, vetro);
        BlackSmithGUI.setItem(16, vetro);
        BlackSmithGUI.setItem(17, vetro);
        BlackSmithGUI.setItem(18, vetro);
        BlackSmithGUI.setItem(19, vetro);
        BlackSmithGUI.setItem(20, vetro);
        BlackSmithGUI.setItem(21, vetro);
        BlackSmithGUI.setItem(22, vetro);
        BlackSmithGUI.setItem(23, vetro);
        BlackSmithGUI.setItem(24, vetro);
        BlackSmithGUI.setItem(25, vetro);
        BlackSmithGUI.setItem(26, vetro);
    }

    public void show (Player p) {
        Main.instance.BlackSmithInventory.put(p, BlackSmithGUI);
        p.openInventory(BlackSmithGUI);
    }

}
