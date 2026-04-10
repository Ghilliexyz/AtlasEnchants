package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateRandomCustomEnchant;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class UpgradeRewardGUI extends Gui {

    private Main main;
    private Player player;
    private String rarity;

    public UpgradeRewardGUI(Main main, Player player, String rarity) {
        // Directly pass the fetched values to super()
        super(player,
                Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.Title")), 27);

        this.player = player;
        this.rarity = rarity;

        // Continue with the rest of your constructor logic
        this.main = main;
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
        String GlassTitle = main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.Filler-Title");
        Material GlassConfigItem = Material.valueOf(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.Filler-Item"));
        ItemStack GlassItem = new ItemStack(GlassConfigItem);
        ItemMeta GlassItemMeta = GlassItem.getItemMeta();
        // Set Title \\
        String GlassItemDisplayName = Main.color(GlassTitle).replace("{Player}", player.getName());
        String GlassItemDisplayNamePAPISet = main.setPlaceholders(player, GlassItemDisplayName);
        if (GlassItemMeta == null) return;
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

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Get the inventory title
        String title = event.getView().getTitle();
        // Get the UpgradeReward Menu title from the config
        String upgradeRewardMenuTitle = Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.Title"));
        // Check if the clicked inventory matches your custom GUI title
        if (title.equals(Main.color(upgradeRewardMenuTitle))) {
            // Check if the clicked inventory is the custom GUI, not the player's inventory
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                int slot = event.getSlot();

                // Cancel clicks on specific slots in the GUI
                if (slot >= 0 && slot <= 26 && slot != 13) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Get the inventory title
        String title = event.getView().getTitle();
        // Check if the title matches your custom GUI title
        if (title.equals(Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.Title")))) {  // Replace with your actual GUI title

            // Define the slots where players can place their items (e.g., 13)
            int[] validSlots = {13};

            // Loop through the defined valid slots and return the items to the player
            for (int slot : validSlots) {
                ItemStack item = inventory.getItem(slot);

                if (item != null && item.getType() != Material.AIR) {
                    // Try adding the item back to the player's inventory
                    HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(item);

                    // If the player's inventory is full, drop the items at their feet
                    if (!remainingItems.isEmpty()) {
                        for (ItemStack remainingItem : remainingItems.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), remainingItem);
                        }
                    }
                }
            }
        }
    }
}
