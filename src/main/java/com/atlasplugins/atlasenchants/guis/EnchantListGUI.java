package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EnchantListGUI extends Gui {

    private final Main main;
    private final Player player;

    private String raritySelected = "";

    private final Map<Integer, String> raritySlotMap = new HashMap<>();

    public EnchantListGUI(Main main, Player player) {
        super(player, Main.color(main.getMenusConfig().getString("EnchantList-Gui.RarityList-Menu.Title")), 27);
        this.main = main;
        this.player = player;
    }

    @Override
    public void setupItems() {
        setupFiller();

        String configPath = "EnchantList-Gui.RarityList-Menu.Rarities";

        if (main.getMenusConfig().getConfigurationSection(configPath) == null) return;
        Set<String> rarityKeys = main.getMenusConfig().getConfigurationSection(configPath).getKeys(false);

        for (String rarity : rarityKeys) {
            String path = configPath + "." + rarity;

            // Get slot and Icon
            int slot = main.getMenusConfig().getInt(path + ".Slot");
            String displayName = main.getMenusConfig().getString(path + ".Display-Name");

            Material icon;
            try {
                icon = Material.valueOf(main.getMenusConfig().getString(path + ".Item"));
            } catch (IllegalArgumentException e) {
                continue;
            }

            if(displayName == null) continue;

            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();

            if(meta != null)
            {
                meta.setDisplayName(Main.color(displayName.replace("{rarityColor}", Main.getRarityColorCode(main, rarity))));

                List<String> enchantmentLore = new ArrayList<>();
                List<String> loreList = main.getMenusConfig().getStringList(path + ".Lore");

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
            raritySlotMap.put(slot, rarity);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Get the inventory title
        String title = event.getView().getTitle();
        // Get the EnchantListGUI Menu title from the config
        String EnchantListGUIMenuTitle = Main.color(main.getMenusConfig().getString("EnchantList-Gui.RarityList-Menu.Title"));

        if(!title.equals(Main.color(EnchantListGUIMenuTitle))) return;
        if(event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        int slot = event.getSlot();
        if(slot >= 0 && slot <= 26) {
            event.setCancelled(true);
        }

        // Handle Dynamically
        if(raritySlotMap.containsKey(slot)){
            raritySelected = raritySlotMap.get(slot);
            GuiManager.setRarity(player.getUniqueId(), raritySelected);
            main.openEnchantRarityListGUI(player, raritySelected);
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
