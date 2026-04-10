package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCustomEnchant;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EnchantRarityListGUI extends Gui {

    private final Main main;
    private final Player player;
    private final String rarity;

    private String raritySelected = "RARE";

    private final Map<Integer, List<String>> enchantmentsByLevel = new TreeMap<>();
    private int currentPage = 0;

    public EnchantRarityListGUI(Main main, Player player, String rarity) {
        super(player, Main.color(buildTitle(main, rarity)), 54);

        this.main = main;
        this.player = player;
        this.rarity = rarity.toUpperCase();

        loadEnchantments();
    }

    private static String buildTitle(Main main, String rarity) {
        return Objects.requireNonNull(main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.Title"))
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

        raritySelected = GuiManager.getRarity(player.getUniqueId());

        // Back button
        ItemStack backBtn = new ItemStack(Material.valueOf(main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.Back-Button.Material")));
        ItemMeta backMeta = backBtn.getItemMeta();
        backMeta.setDisplayName(Main.color(main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.Back-Button.Title")));

        List<String> backButtonLore = new ArrayList<>();
        for (String lore : main.getMenusConfig().getStringList("EnchantList-Gui.EnchantList-Menu.Back-Button.Lore")) {
            backButtonLore.add(Main.color(main.setPlaceholders(player, lore)));
        }
        backMeta.setLore(backButtonLore);
        backBtn.setItemMeta(backMeta);
        inventory.setItem(0, backBtn);

        // Get enchantments for this rarity
        List<String> enchantNames = new ArrayList<>();
        for (String enchantName : main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false)) {
            if (main.getEnchantmentsConfig().getString("Enchantments." + enchantName + ".Enchantment-Rarity", "").equalsIgnoreCase(rarity)) {
                enchantNames.add(enchantName);
            }
        }
        enchantNames.sort(String::compareToIgnoreCase);

        int maxRows = 4;
        int maxCols = 7;
        int totalPages = Math.max(1, (int) Math.ceil(enchantNames.size() / (double) maxCols));

        // Pagination controls
        if (currentPage > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta = prevPage.getItemMeta();
            meta.setDisplayName(Main.color("&aPrevious Page"));
            prevPage.setItemMeta(meta);
            inventory.setItem(18, prevPage);
        }

        if (currentPage < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta = nextPage.getItemMeta();
            meta.setDisplayName(Main.color("&aNext Page"));
            nextPage.setItemMeta(meta);
            inventory.setItem(26, nextPage);
        }

        // Slots where enchants go (grid layout)
        List<Integer> validSlots = generateGridSlots(1, 1, maxRows, maxCols, 9);

        int enchantIndex = currentPage * maxCols;

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

    @Override
    public void handleClick(InventoryClickEvent event) {
        // Get the inventory title
        String title = event.getView().getTitle();
        // Get the item clicked event
        ItemStack clicked = event.getCurrentItem();
        // Get the PersistentData
        PersistentDataContainer container = null;
        // Get the item meta
        ItemMeta meta = null;
        if (clicked != null) {
            meta = clicked.getItemMeta();
        }
        if (meta != null) {
            container = meta.getPersistentDataContainer();
        }
        String EnchantRarityListGUI = Main.color(main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.Title"))
                .replace("{rarityColor}", Main.getRarityColorCode(main, raritySelected))
                .replace("{rarityName}", raritySelected);
        // Check if the clicked inventory matches your custom GUI title
        if (title.equals(Main.color(EnchantRarityListGUI))) {
            // Check if the clicked inventory is the custom GUI, not the player's inventory
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                int slot = event.getSlot();

                // Cancel clicks on specific slots in the GUI
                if (slot >= 0 && slot <= 54) {
                    event.setCancelled(true);
                }

                // Handle clicks within your custom GUI
                if (slot == 0) {
                    main.openEnchantListGUI(player);
                    return;
                }

                // Pagination
                if (slot == 18 && currentPage > 0) {
                    currentPage--;
                    setupItems();
                    return;
                }
                if (slot == 26 && currentPage < Math.max(1, (int) Math.ceil(enchantsWithLevels.size() / 7.0)) - 1) {
                    currentPage++;
                    setupItems();
                    return;
                }

                if (clicked == null) return;

                // Handle clicks on the enchantment items
                if (!clicked.hasItemMeta()) return;

                // Check if the sender does not have the permission and is not an operator
                if (!player.hasPermission("atlasenchants.enchantlistgrabber") && !player.isOp()) {
                    // Send noPermission Message in chat when called.
                    if (main.getMenusConfig().getBoolean("EnchantList-Gui.RarityList-Menu.Grabber-Message")) {
                        for (String noPermission : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-NoPermissions")) {
                            String withPAPISet = main.setPlaceholders((Player) player, noPermission);
                            player.sendMessage(Main.color(withPAPISet));
                        }
                    }
                    return;
                }

                NamespacedKey nameKey = new NamespacedKey(main, "enchant_name");
                NamespacedKey levelKey = new NamespacedKey(main, "enchant_level");

                if (container.has(nameKey, PersistentDataType.STRING) && container.has(levelKey, PersistentDataType.INTEGER)) {
                    String enchantName = container.get(nameKey, PersistentDataType.STRING);
                    int enchantLevel = container.get(levelKey, PersistentDataType.INTEGER);

                    CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
                    createCustomEnchant.CreateCustomEnchantmentItem(enchantName, enchantLevel, 1, player);
                }
            }
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        GuiManager.clear(event.getPlayer().getUniqueId());
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
