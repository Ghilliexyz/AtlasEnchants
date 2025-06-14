package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantListGUI extends Gui {

    private final Main main;
    private final Player player;

    private String raritySelected = "";
    // Map rarity name to material icon (customize icons as you want)
    private final Map<String, Material> rarityIcons = new HashMap<>();

    public EnchantListGUI(Main main, Player player) {
        super(player, Main.color(main.getMenusConfig().getString("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Title")), 27);
        this.main = main;
        this.player = player;
        rarityIcons.put("GODLY", Material.valueOf(main.getMenusConfig().getString("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.GODLY.Item")));
        rarityIcons.put("LEGENDARY", Material.valueOf(main.getMenusConfig().getString("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.LEGENDARY.Item")));
        rarityIcons.put("EPIC", Material.valueOf(main.getMenusConfig().getString("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.EPIC.Item")));
        rarityIcons.put("RARE", Material.valueOf(main.getMenusConfig().getString("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.RARE.Item")));
        setupItems();
    }

    @Override
    public void setupItems() {
        setupFiller();

//        int slot = 10; // Start slot to place rarity items

        for (Map.Entry<String, Material> entry : rarityIcons.entrySet()) {
            String rarity = entry.getKey();
            Material icon = entry.getValue();

            int slot = main.getMenusConfig().getInt("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities." + rarity + ".Slot");

            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(Main.color(Main.getRarityColorCode(main, rarity) + rarity));

                ArrayList<String> enchantmentLore = new ArrayList<>();
                List<String> loreList = main.getMenusConfig().getStringList("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities." + rarity + ".Lore");

                for (String lore : loreList) {
                    String colored = Main.color(lore)
                            .replace("{rarityColor}", Main.getRarityColorCode(main, rarity))
                            .replace("{rarityName}", rarity.toUpperCase());

                    String withPAPI = main.setPlaceholders(player, colored);
                    enchantmentLore.add(Main.color(withPAPI));
                }

                meta.setLore(enchantmentLore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Get the inventory title
        String title = event.getView().getTitle();
        // Get the EnchantListGUI Menu title from the config
        String EnchantListGUIMenuTitle = Main.color(main.getMenusConfig().getString("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Title"));
        // Check if the clicked inventory matches your custom GUI title
        if (title.equals(Main.color(EnchantListGUIMenuTitle))) {
            // Check if the clicked inventory is the custom GUI, not the player's inventory
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                int slot = event.getSlot();

                // Cancel clicks on specific slots in the GUI
                if (slot >= 0 && slot <= 26) {
                    event.setCancelled(true);
                }

                // Handle clicks within your custom GUI
                if (slot == main.getMenusConfig().getInt("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.GODLY.Slot")) {
                    raritySelected = "GODLY";
                    GuiManager.setRarity(player.getUniqueId(), raritySelected);
                    main.openEnchantRarityListGUI(player, raritySelected);
                }
                if (slot == main.getMenusConfig().getInt("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.LEGENDARY.Slot")) {
                    raritySelected = "LEGENDARY";
                    GuiManager.setRarity(player.getUniqueId(), raritySelected);
                    main.openEnchantRarityListGUI(player, raritySelected);
                }
                if (slot == main.getMenusConfig().getInt("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.EPIC.Slot")) {
                    raritySelected = "EPIC";
                    GuiManager.setRarity(player.getUniqueId(), raritySelected);
                    main.openEnchantRarityListGUI(player, raritySelected);
                }
                if (slot == main.getMenusConfig().getInt("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.RARE.Slot")) {
                    raritySelected = "RARE";
                    GuiManager.setRarity(player.getUniqueId(), raritySelected);
                    main.openEnchantRarityListGUI(player, raritySelected);
                }
            }
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {

    }

    private void setupFiller() {
        String fillerTitle = main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.Filler-Title", "&0_");
        Material fillerItem;
        try {
            fillerItem = Material.valueOf(main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.Filler-Item", "BLACK_STAINED_GLASS_PANE"));
        } catch (IllegalArgumentException e) {
            fillerItem = Material.BLACK_STAINED_GLASS_PANE;
        }

        ItemStack item = new ItemStack(fillerItem);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Main.color(fillerTitle));
            item.setItemMeta(meta);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, item);
        }
    }
}
