package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EnchantRarityListGUI extends Gui {

    private final Main main;
    private final Player player;
    private final String rarity;

    private final Map<Integer, List<String>> enchantmentsByLevel = new TreeMap<>();
    private int currentPage = 0;
    private final int ITEMS_PER_PAGE = 21; // Slots available for enchants

    public EnchantRarityListGUI(Main main, Player player, String rarity) {
        super(player, Main.color(buildTitle(main, rarity)), 54);

        this.main = main;
        this.player = player;
        this.rarity = rarity.toUpperCase();

        loadEnchantments();
        setupItems();
    }

    private static String buildTitle(Main main, String rarity) {
        return Objects.requireNonNull(main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.EnchantList-Menu-Title"))
                .replace("{rarityColor}", Main.getRarityColorCode(main, rarity))
                .replace("{rarityName}", rarity);
    }

    private final LinkedHashMap<String, List<Integer>> enchantsWithLevels = new LinkedHashMap<>();

    private void loadEnchantments() {
        enchantsWithLevels.clear();

        for (String enchantName : main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false)) {
            String enchantRarity = main.getEnchantmentsConfig().getString("Enchantments." + enchantName + ".Enchantment-Rarity", "").toUpperCase();
            if (!enchantRarity.equals(rarity)) continue;

            int maxLevel = main.getEnchantmentsConfig().getInt("Enchantments." + enchantName + ".Enchantment-MaxLvl", 1);
            List<Integer> levels = new ArrayList<>();
            for (int lvl = 1; lvl <= maxLevel; lvl++) {
                levels.add(lvl);
            }
            enchantsWithLevels.put(enchantName, levels);
        }
    }

    @Override
    public void setupItems() {
        inventory.clear();

        setupFiller(); // Fill entire inventory with glass first


        // Back button
        ItemStack backBtn = new ItemStack(Material.valueOf(main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.EnchantList-Menu-Back-Button.Material")));
        ItemMeta backMeta = backBtn.getItemMeta();
        backMeta.setDisplayName(Main.color(main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.EnchantList-Menu-Back-Button.Title")));

        List<String> backButtonLore = new ArrayList<>();
        for (String lore : main.getMenusConfig().getStringList("EnchantList-Gui.EnchantList-Menu.EnchantList-Menu-Back-Button.Lore")) {
            backButtonLore.add(Main.color(main.setPlaceholders(player, lore)));
        }
        backMeta.setLore(backButtonLore);
        backBtn.setItemMeta(backMeta);
        inventory.setItem(0, backBtn);

        // Pagination controls
        if (currentPage > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta = prevPage.getItemMeta();
            meta.setDisplayName(Main.color("&aPrevious Page"));
            prevPage.setItemMeta(meta);
            inventory.setItem(18, prevPage);
        }

        // Next page logic if needed
        int enchantCount = 0;
        for (String enchantName : main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false)) {
            if (main.getEnchantmentsConfig().getString("Enchantments." + enchantName + ".Enchantment-Rarity", "").equalsIgnoreCase(rarity)) {
                enchantCount++;
            }
        }
        int totalPages = (int) Math.ceil(enchantCount / (double) ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta = nextPage.getItemMeta();
            meta.setDisplayName(Main.color("&aNext Page"));
            nextPage.setItemMeta(meta);
            inventory.setItem(26, nextPage);
        }

        // Get enchantments
        List<String> enchantNames = new ArrayList<>();
        for (String enchantName : main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false)) {
            if (main.getEnchantmentsConfig().getString("Enchantments." + enchantName + ".Enchantment-Rarity", "").equalsIgnoreCase(rarity)) {
                enchantNames.add(enchantName);
            }
        }
        enchantNames.sort(String::compareToIgnoreCase);

        // Slots where enchants go (start at slot 10, skip border)
        List<Integer> validSlots = generateGridSlots(1, 1, 4, 7, 9); // Assuming 9 columns in inventory grid

        int maxRows = 4;
        int maxCols = 7;

        int enchantIndex = 0;

        outer:
        for (int col = 0; col < maxCols; col++) {
            if (enchantIndex >= enchantNames.size()) break;

            String enchantName = enchantNames.get(enchantIndex++);
            List<Integer> levels = enchantsWithLevels.get(enchantName);

            for (int row = 0; row < maxRows; row++) {
                if (row >= levels.size()) continue;

                int slot = validSlots.get(row * maxCols + col);
                int level = levels.get(row);
                ItemStack enchantItem = createEnchantItem(enchantName, level);
                inventory.setItem(slot, enchantItem);
            }
        }
    }

    private List<Integer> generateGridSlots(int startRow, int startCol, int rows, int cols, int totalColumns) {
        List<Integer> slots = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                slots.add((startRow + row) * totalColumns + (startCol + col));
            }
        }
        return slots;
    }

    private void setupFiller() {
        String fillerTitle = main.getSettingsConfig().getString("EnchantList-Gui.EnchantList-Menu.Filler-Title", "&0_");
        Material fillerItem;
        try {
            fillerItem = Material.valueOf(main.getSettingsConfig().getString("EnchantList-Gui.EnchantList-Menu.Filler-Item", "BLACK_STAINED_GLASS_PANE"));
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
            // Avoid overwriting navigation & enchant slots
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item);
            }
        }
    }

    private ItemStack createEnchantItem(String enchantName, int level) {
        String basePath = "Enchantments." + enchantName + ".";

        List<String> applyItems = main.getEnchantmentsConfig().getStringList(basePath + "Enchantment-Apply-Item");
        Material displayMaterial = Material.ENCHANTED_BOOK;
        if (!applyItems.isEmpty()) {
            try {
                displayMaterial = Material.valueOf(applyItems.get(0));
            } catch (IllegalArgumentException e) {
                displayMaterial = Material.ENCHANTED_BOOK;
            }
        }

        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        String title = main.getEnchantmentsConfig().getString(basePath + "Enchantment-Title", enchantName);
        List<String> loreLines = main.getEnchantmentsConfig().getStringList(basePath + "Enchantment-Lore");

        List<String> loreColored = new ArrayList<>();
        for (String line : loreLines) {
            loreColored.add(main.applyPlaceholders(Main.color(line), main, enchantName, level));
        }

        NamespacedKey nameKey = new NamespacedKey(main, "enchant_name");
        NamespacedKey levelKey = new NamespacedKey(main, "enchant_level");

        meta.getPersistentDataContainer().set(nameKey, PersistentDataType.STRING, enchantName);
        meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);

        meta.setDisplayName(Main.color(title));
        meta.setLore(loreColored);
        item.setItemMeta(meta);

        return item;
    }
}
