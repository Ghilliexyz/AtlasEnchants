package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateRandomCustomEnchant;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UpgradeRewardGUI extends Gui {

    private Main main;
    private Player player;
    private String rarity;

    public UpgradeRewardGUI(Main main, Player player, String rarity) {
        // Directly pass the fetched values to super()
        super(player,
                Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.UpgradeReward-Menu-Title")), 27);

        this.player = player;
        this.rarity = rarity;

        // Continue with the rest of your constructor logic
        this.main = main;
        setupItems();
    }



    @Override
    public void setupItems() {
        // Set up the specific items for this GUI \\
        // ---------- Upgrade Reward ---------- \\
        // Create Item \\
        CreateRandomCustomEnchant createRandomCustomEnchant = new CreateRandomCustomEnchant(main);
        ItemStack UpgradeRewardItem = createRandomCustomEnchant.CreateRandomCustomEnchantmentItem(player, 1, false, rarity);
        // Place Items in correct slots \\
        inventory.setItem(13, UpgradeRewardItem);

        // ---------- GLASS FILLER ---------- \\
        // Create Item \\
        String GlassTitle = main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.UpgradeReward-Menu-Filler-Title");
        Material GlassConfigItem = Material.valueOf(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.UpgradeReward-Menu-Filler-Item"));
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
        inventory.setItem(11, GlassItem);
        inventory.setItem(12, GlassItem);
        inventory.setItem(14, GlassItem);
        inventory.setItem(15, GlassItem);
        inventory.setItem(16, GlassItem);
        inventory.setItem(17, GlassItem);
        inventory.setItem(18, GlassItem);
        inventory.setItem(19, GlassItem);
        inventory.setItem(20, GlassItem);
        inventory.setItem(21, GlassItem);
        inventory.setItem(22, GlassItem);
        inventory.setItem(23, GlassItem);
        inventory.setItem(24, GlassItem);
        inventory.setItem(25, GlassItem);
        inventory.setItem(26, GlassItem);
    }
}
