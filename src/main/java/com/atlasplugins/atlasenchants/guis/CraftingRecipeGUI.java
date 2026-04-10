package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CraftingRecipeGUI extends Gui {

    private final Main main;
    private final Player player;
    private final String recipeType; // "altar" or "anvil"

    // 3x3 crafting grid slots in a 45-slot inventory
    private static final int[] CRAFTING_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int ARROW_SLOT = 23;
    private static final int RESULT_SLOT = 25;
    private static final int BACK_SLOT = 36;

    public CraftingRecipeGUI(Main main, Player player, String recipeType) {
        super(player, buildTitle(main, recipeType), 45);
        this.main = main;
        this.player = player;
        this.recipeType = recipeType;
    }

    private static String buildTitle(Main main, String recipeType) {
        String title = main.getMenusConfig().getString("Guide-Gui.Recipe-Menu.Title", "&c&lCrafting Recipe");
        String name = recipeType.equals("altar") ? "Altar of Circe" : "Circe's Anvil";
        return Main.color(title.replace("{recipeName}", name));
    }

    @Override
    public void setupItems() {
        setupFiller();
        setupCraftingGrid();
        setupArrow();
        setupResult();
        setupBackButton();
    }

    private void setupCraftingGrid() {
        String configPrefix;
        if (recipeType.equals("altar")) {
            configPrefix = "AltarOfCirce.AltarOfCirce-Crafting-";
        } else {
            configPrefix = "CircesAnvil.CircesAnvil-Crafting-";
        }

        String row1 = main.getEnchantmentsConfig().getString(configPrefix + "Row-1", "   ");
        String row2 = main.getEnchantmentsConfig().getString(configPrefix + "Row-2", "   ");
        String row3 = main.getEnchantmentsConfig().getString(configPrefix + "Row-3", "   ");

        String pattern = row1 + row2 + row3;

        for (int i = 0; i < 9 && i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            ItemStack item = resolveIngredient(c, configPrefix);
            if (item != null) {
                inventory.setItem(CRAFTING_SLOTS[i], item);
            }
        }
    }

    private ItemStack resolveIngredient(char c, String configPrefix) {
        if (c == ' ') return null;

        if (c == 'X') {
            // Oracle Book
            String name = main.getEnchantmentsConfig().getString("OraclesOfEnchantment.OraclesOfEnchantment-DisplayName", "&b&lOracle Of Enchantment");
            String matName = main.getEnchantmentsConfig().getString("OraclesOfEnchantment.OraclesOfEnchantment-Item", "WRITTEN_BOOK");
            Material mat;
            try {
                mat = Material.valueOf(matName);
            } catch (IllegalArgumentException e) {
                mat = Material.WRITTEN_BOOK;
            }
            return createDisplayItem(mat, name);
        }

        if (c == 'Y') {
            // Scrap of Circe's Weave
            String name = main.getEnchantmentsConfig().getString("ScrapOfCirceWeave.ScrapOfCirceWeave-DisplayName", "&3&lScrap Of Circe's Weave");
            String matName = main.getEnchantmentsConfig().getString("ScrapOfCirceWeave.ScrapOfCirceWeave-Item", "PAPER");
            Material mat;
            try {
                mat = Material.valueOf(matName);
            } catch (IllegalArgumentException e) {
                mat = Material.PAPER;
            }
            return createDisplayItem(mat, name);
        }

        if (c == 'Z') {
            // Circe's Ember
            String name = main.getEnchantmentsConfig().getString("CircesEmber.CircesEmber-DisplayName", "&6&lCirce's Ember");
            String matName = main.getEnchantmentsConfig().getString("CircesEmber.CircesEmber-Item", "BLAZE_POWDER");
            Material mat;
            try {
                mat = Material.valueOf(matName);
            } catch (IllegalArgumentException e) {
                mat = Material.BLAZE_POWDER;
            }
            return createDisplayItem(mat, name);
        }

        // Regular material (A-I)
        String matName = main.getEnchantmentsConfig().getString(configPrefix + "Materials-" + c);
        if (matName == null) return null;
        Material material = Material.matchMaterial(matName);
        if (material == null) return null;
        return new ItemStack(material);
    }

    private ItemStack createDisplayItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Main.color(displayName));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void setupArrow() {
        String arrowItem = main.getMenusConfig().getString("Guide-Gui.Recipe-Menu.Arrow.Item", "ARROW");
        String arrowTitle = main.getMenusConfig().getString("Guide-Gui.Recipe-Menu.Arrow.Title", "&7&l>>>");

        Material mat;
        try {
            mat = Material.valueOf(arrowItem);
        } catch (IllegalArgumentException e) {
            mat = Material.ARROW;
        }

        ItemStack arrow = new ItemStack(mat);
        ItemMeta meta = arrow.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Main.color(arrowTitle));
            arrow.setItemMeta(meta);
        }
        inventory.setItem(ARROW_SLOT, arrow);
    }

    private void setupResult() {
        ItemStack result;
        if (recipeType.equals("altar")) {
            CreateAltarOfCirce creator = new CreateAltarOfCirce(main);
            result = creator.CreateAltarOfCirceItem(1, null);
        } else {
            CreateCircesAnvil creator = new CreateCircesAnvil(main);
            result = creator.CreateCircesAnvilItem(1, null);
        }
        inventory.setItem(RESULT_SLOT, result);
    }

    private void setupBackButton() {
        String backMaterial = main.getMenusConfig().getString("Guide-Gui.Recipe-Menu.Back-Button.Material", "RED_STAINED_GLASS_PANE");
        String backTitle = main.getMenusConfig().getString("Guide-Gui.Recipe-Menu.Back-Button.Title", "&cBack");

        Material mat;
        try {
            mat = Material.valueOf(backMaterial);
        } catch (IllegalArgumentException e) {
            mat = Material.RED_STAINED_GLASS_PANE;
        }

        ItemStack back = new ItemStack(mat);
        ItemMeta meta = back.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Main.color(backTitle));

            List<String> loreList = main.getMenusConfig().getStringList("Guide-Gui.Recipe-Menu.Back-Button.Lore");
            if (!loreList.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : loreList) {
                    coloredLore.add(Main.color(line));
                }
                meta.setLore(coloredLore);
            }

            back.setItemMeta(meta);
        }
        inventory.setItem(BACK_SLOT, back);
    }

    private void setupFiller() {
        String fillerTitle = main.getMenusConfig().getString("Guide-Gui.Recipe-Menu.Filler-Title", "&0_");
        Material fillerItem;
        try {
            fillerItem = Material.valueOf(main.getMenusConfig().getString("Guide-Gui.Recipe-Menu.Filler-Item", "BLACK_STAINED_GLASS_PANE"));
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

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        event.setCancelled(true);

        if (event.getSlot() == BACK_SLOT) {
            Bukkit.getScheduler().runTask(main, () -> main.openGuideGUI(player));
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {

    }
}
