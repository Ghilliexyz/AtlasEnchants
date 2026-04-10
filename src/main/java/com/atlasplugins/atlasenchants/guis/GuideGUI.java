package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GuideGUI extends Gui {

    private final Main main;
    private final Player player;

    public GuideGUI(Main main, Player player) {
        super(player, Main.color(main.getMenusConfig().getString("Guide-Gui.Guide-Menu.Title")), 27);
        this.main = main;
        this.player = player;
    }

    @Override
    public void setupItems() {
        setupFiller();

        String configPath = "Guide-Gui.Guide-Menu.Items";

        if (main.getMenusConfig().getConfigurationSection(configPath) == null) return;
        Set<String> itemKeys = main.getMenusConfig().getConfigurationSection(configPath).getKeys(false);

        for (String key : itemKeys) {
            String path = configPath + "." + key;

            int slot = main.getMenusConfig().getInt(path + ".Slot");
            String displayName = main.getMenusConfig().getString(path + ".Display-Name");

            Material icon;
            try {
                icon = Material.valueOf(main.getMenusConfig().getString(path + ".Item"));
            } catch (IllegalArgumentException e) {
                continue;
            }

            if (displayName == null) continue;

            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(Main.color(displayName));

                List<String> enchantmentLore = new ArrayList<>();
                List<String> loreList = main.getMenusConfig().getStringList(path + ".Lore");

                // Pre-compute dynamic config values for guide placeholders
                String oblivionChance = String.valueOf((int) (main.getEnchantmentsConfig().getDouble("OblivionShard.OblivionShard-ReturnEnchant-Chance") * 100));
                String requiredBooks = String.valueOf(Math.max(1, Math.min(10, main.getSettingsConfig().getInt("UpgradeEnchantSettings.Required-Books", 10))));
                String scrapChance = String.valueOf((int) (main.getEnchantmentsConfig().getDouble("ScrapOfCirceWeave.ScrapOfCirceWeave-Success-Chance") * 100));

                for (String lore : loreList) {
                    String withPAPI = main.setPlaceholders(player, lore);
                    withPAPI = withPAPI.replace("{oblivionShardChance}", oblivionChance);
                    withPAPI = withPAPI.replace("{requiredBooks}", requiredBooks);
                    withPAPI = withPAPI.replace("{scrapSuccessChance}", scrapChance);
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
        String title = event.getView().getTitle();
        String guideMenuTitle = Main.color(main.getMenusConfig().getString("Guide-Gui.Guide-Menu.Title"));

        if (!title.equals(guideMenuTitle)) return;
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        int slot = event.getSlot();
        if (slot >= 0 && slot <= 26) {
            event.setCancelled(true);
        }

        // Open crafting recipe GUI when clicking Altar or Anvil
        int altarSlot = main.getMenusConfig().getInt("Guide-Gui.Guide-Menu.Items.Altar.Slot", -1);
        int anvilSlot = main.getMenusConfig().getInt("Guide-Gui.Guide-Menu.Items.Anvil.Slot", -1);

        if (slot == altarSlot) {
            Bukkit.getScheduler().runTask(main, () -> {
                CraftingRecipeGUI recipeGUI = new CraftingRecipeGUI(main, player, "altar");
                recipeGUI.open();
            });
        } else if (slot == anvilSlot) {
            Bukkit.getScheduler().runTask(main, () -> {
                CraftingRecipeGUI recipeGUI = new CraftingRecipeGUI(main, player, "anvil");
                recipeGUI.open();
            });
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {

    }

    private void setupFiller() {
        String fillerTitle = main.getMenusConfig().getString("Guide-Gui.Guide-Menu.Filler-Title", "&0_");
        Material fillerItem;
        try {
            fillerItem = Material.valueOf(main.getMenusConfig().getString("Guide-Gui.Guide-Menu.Filler-Item", "BLACK_STAINED_GLASS_PANE"));
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
