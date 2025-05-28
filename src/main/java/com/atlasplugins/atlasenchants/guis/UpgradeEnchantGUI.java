package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class UpgradeEnchantGUI extends Gui {

    private Main main;
    private Player player;

    public UpgradeEnchantGUI(Main main, Player player) {
        // Directly pass the fetched values to super()
        super(player,
                Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Title")), 54);

        this.player = player;

        // Continue with the rest of your constructor logic
        this.main = main;
        setupItems();
    }

    @Override
    public void setupItems() {
        // ---------- Upgrade Enchant Btn ---------- \\
//        boolean yeshello = true;

//        if(yeshello)
//        {
            // Create Item \\
            String UpgradeAcceptableBtnTitle = main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-Acceptable-Title");
            Material UpgradeAcceptableBtnConfigItem = Material.valueOf(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-Acceptable-Item"));
            ItemStack UpgradeAcceptableBtnItem = new ItemStack(UpgradeAcceptableBtnConfigItem);
            ItemMeta UpgradeAcceptableBtnItemMeta = UpgradeAcceptableBtnItem.getItemMeta();
            // Set Title \\
            String UpgradeAcceptableBtnItemDisplayName = Main.color(UpgradeAcceptableBtnTitle).replace("{Player}", player.getName());
            String UpgradeAcceptableBtnItemDisplayNamePAPISet = main.setPlaceholders(player, UpgradeAcceptableBtnItemDisplayName);
            assert UpgradeAcceptableBtnItemMeta != null;
            // Set Lore \\
            ArrayList<String> UpgradeAcceptableBtnLore = new ArrayList<>();
            for (String WorldInfo : main.getMenusConfig().getStringList("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-Acceptable-Lore")) {
                String withPAPISet = main.setPlaceholders(player, WorldInfo);
                UpgradeAcceptableBtnLore.add(Main.color(withPAPISet));
            }
            // Set all values \\
            UpgradeAcceptableBtnItemMeta.setLore(UpgradeAcceptableBtnLore);
            UpgradeAcceptableBtnItemMeta.setDisplayName(Main.color(UpgradeAcceptableBtnItemDisplayNamePAPISet));
            UpgradeAcceptableBtnItem.setItemMeta(UpgradeAcceptableBtnItemMeta);
            // Place Items in correct slots \\
            inventory.setItem(40, UpgradeAcceptableBtnItem);
//        }
//        else {
//            // Create Item \\
//            String UpgradeNotAcceptableBtnTitle = main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-NotAcceptable-Title");
//            Material UpgradeNotAcceptableBtnConfigItem = Material.valueOf(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-NotAcceptable-Item"));
//            ItemStack UpgradeNotAcceptableBtnItem = new ItemStack(UpgradeNotAcceptableBtnConfigItem);
//            ItemMeta UpgradeNotAcceptableBtnItemMeta = UpgradeNotAcceptableBtnItem.getItemMeta();
//            // Set Title \\
//            String UpgradeNotAcceptableBtnItemDisplayName = Main.color(UpgradeNotAcceptableBtnTitle).replace("{Player}", player.getName());
//            String UpgradeNotAcceptableBtnItemDisplayNamePAPISet = main.setPlaceholders(player, UpgradeNotAcceptableBtnItemDisplayName);
//            assert UpgradeNotAcceptableBtnItemMeta != null;
//            // Set Lore \\
//            ArrayList<String> UpgradeNotAcceptableBtnLore = new ArrayList<>();
//            for (String WorldInfo : main.getMenusConfig().getStringList("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-NotAcceptable-Lore")) {
//                String withPAPISet = main.setPlaceholders(player, WorldInfo);
//                UpgradeNotAcceptableBtnLore.add(Main.color(withPAPISet));
//            }
//            // Set all values \\
//            UpgradeNotAcceptableBtnItemMeta.setLore(UpgradeNotAcceptableBtnLore);
//            UpgradeNotAcceptableBtnItemMeta.setDisplayName(Main.color(UpgradeNotAcceptableBtnItemDisplayNamePAPISet));
//            UpgradeNotAcceptableBtnItem.setItemMeta(UpgradeNotAcceptableBtnItemMeta);
//            // Place Items in correct slots \\
//            inventory.setItem(40, UpgradeNotAcceptableBtnItem);
//        }

        // ---------- GLASS FILLER ---------- \\
        // Create Item \\
        String GlassTitle = main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Filler-Title");
        Material GlassConfigItem = Material.valueOf(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Filler-Item"));
        ItemStack GlassItem = new ItemStack(GlassConfigItem);
        ItemMeta GlassItemMeta = GlassItem.getItemMeta();
        // Set Title \\
        String GlassItemDisplayName = Main.color(GlassTitle).replace("{Player}", player.getName());
        String GlassItemDisplayNamePAPISet = main.setPlaceholders(player, GlassItemDisplayName);
        assert GlassItemMeta != null;
        GlassItemMeta.setDisplayName(Main.color(GlassItemDisplayNamePAPISet));
        GlassItem.setItemMeta(GlassItemMeta);
        // Place Items in correct slots \\
        inventory.setItem(0, GlassItem);
        inventory.setItem(1, GlassItem);
        inventory.setItem(2, GlassItem);
        inventory.setItem(3, GlassItem);
        inventory.setItem(4, GlassItem);
        inventory.setItem(5, GlassItem);
        inventory.setItem(6, GlassItem);
        inventory.setItem(7, GlassItem);
        inventory.setItem(8, GlassItem);
        inventory.setItem(9, GlassItem);
        inventory.setItem(10, GlassItem);
        inventory.setItem(16, GlassItem);
        inventory.setItem(17, GlassItem);
        inventory.setItem(18, GlassItem);
        inventory.setItem(19, GlassItem);
        inventory.setItem(25, GlassItem);
        inventory.setItem(26, GlassItem);
        inventory.setItem(27, GlassItem);
        inventory.setItem(28, GlassItem);
        inventory.setItem(29, GlassItem);
        inventory.setItem(30, GlassItem);
        inventory.setItem(31, GlassItem);
        inventory.setItem(32, GlassItem);
        inventory.setItem(33, GlassItem);
        inventory.setItem(34, GlassItem);
        inventory.setItem(35, GlassItem);
        inventory.setItem(36, GlassItem);
        inventory.setItem(37, GlassItem);
        inventory.setItem(38, GlassItem);
        inventory.setItem(39, GlassItem);
        inventory.setItem(41, GlassItem);
        inventory.setItem(42, GlassItem);
        inventory.setItem(43, GlassItem);
        inventory.setItem(44, GlassItem);
        inventory.setItem(45, GlassItem);
        inventory.setItem(46, GlassItem);
        inventory.setItem(47, GlassItem);
        inventory.setItem(48, GlassItem);
        inventory.setItem(49, GlassItem);
        inventory.setItem(50, GlassItem);
        inventory.setItem(51, GlassItem);
        inventory.setItem(52, GlassItem);
        inventory.setItem(53, GlassItem);
    }
}
