package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class GuiListener implements Listener {

    private Main main;
    private boolean hasUpgraded = false;

    private String raritySelected = "";

    public GuiListener(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        hasUpgraded = false;
        // Get the inventory title
        String title = event.getView().getTitle();
        // Get the player who clicked
        Player player = (Player) event.getWhoClicked();
        // Get the item clicked event
        ItemStack clicked = event.getCurrentItem();
        // Get the item meta
        assert clicked != null;
        ItemMeta meta = clicked.getItemMeta();
        // Get the PersistentData
        PersistentDataContainer container = null;
        if(meta != null)
        {
            container = meta.getPersistentDataContainer();
        }

        // ===== Upgrade Enchant Menu ===== \\
        // Get the UpgradeEnchant Menu title from the config
        String upgradeEnchantMenuTitle = Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Title"));
        // Check if the clicked inventory matches your custom GUI title
        if (title.equals(Main.color(upgradeEnchantMenuTitle))) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                int slot = event.getSlot();

                // Cancel clicks on specific slots in the GUI
                if ((slot >= 0 && slot <= 10) || (slot >= 16 && slot <= 19) || (slot >= 25 && slot <= 53)) {
                    event.setCancelled(true);
                }

                // Handle clicks within your custom GUI
//                if (slot == 11) {
//                    player.sendMessage(Main.color("enchant slot 1"));
//                }

                // Check if the upgrade button is clicked (assume slot 40 is the upgrade button)
                if (slot == 40) {
                    Inventory inventory = event.getInventory();
                    String requiredRarity = null;
                    int matchingCount = 0;

                    // Loop through enchantment slots (11 to 24 for example)
                    for (int i = 11; i <= 24; i++) {
                        ItemStack item = inventory.getItem(i);

                        if (item != null && item.hasItemMeta()) {
                            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                            String enchantData = pdc.get(Main.customEnchantKeys, PersistentDataType.STRING);

                            if (enchantData != null) {
                                String[] enchantParts = enchantData.split(":");
                                String enchantName = enchantParts[0];

                                // Get rarity from the config
                                String enchantRarity = main.getEnchantmentsConfig().getString("Enchantments." + enchantName + ".Enchantment-Rarity");

                                if (requiredRarity == null) {
                                    // Set the first enchantment's rarity as the required rarity
                                    requiredRarity = enchantRarity;
                                }

                                // Check if the rarity matches
                                if (enchantRarity != null && enchantRarity.equals(requiredRarity)) {
                                    matchingCount++;
                                }
                            }
                        }
                    }

                    // Check if there are 10 matching enchantments
                    if (matchingCount >= 10) {
                        // Upgrade logic
                        if (requiredRarity != null) {
                            String newRarity = getNextRarity(requiredRarity);
                            main.getLogger().info("next rarity: " + newRarity);
                            if (newRarity != null) {
                                hasUpgraded = true;
                                if(main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.UpgradeEnchant-SuccessUpgrade-Message-Toggle")) {
                                    // Send UpgradeMaxRarity Message in chat when called.
                                    for (String UpgradeMaxRarityMessage : main.getSettingsConfig().getStringList("UpgradeEnchantMessages.UpgradeEnchant-SuccessUpgrade-Message")) {
                                        String withPAPISet = main.setPlaceholders(player, UpgradeMaxRarityMessage);
                                        String message = Main.color(withPAPISet)
                                                .replace("{oldRarity}", requiredRarity)
                                                .replace("{newRarity}", newRarity);
                                        player.sendMessage(message);
                                    }
                                }
                                // Get the bool to check if the user wants to play the blacklisted enchant sound
                                boolean SuccessUpgradePlaySound = main.getSettingsConfig().getBoolean("UpgradeEnchantSounds.UpgradeEnchant-SuccessUpgrade-Sound-Toggle");
                                // check if the user wants to play the Already Applied sound
                                if(SuccessUpgradePlaySound){
                                    // Get apply sound via config.
                                    Sound SuccessUpgradeSound = Sound.valueOf(main.getSettingsConfig().getString("UpgradeEnchantSounds.UpgradeEnchant-SuccessUpgrade-Sound"));
                                    float SuccessUpgradeVolume = main.getSettingsConfig().getInt("UpgradeEnchantSounds.UpgradeEnchant-SuccessUpgrade-Volume");
                                    float SuccessUpgradePitch = main.getSettingsConfig().getInt("UpgradeEnchantSounds.UpgradeEnchant-SuccessUpgrade-Pitch");

                                    // Play sound for when enchant is Already Applied.
                                    player.playSound(player.getLocation(), SuccessUpgradeSound, SuccessUpgradeVolume, SuccessUpgradePitch);
                                }
                                // open the reward GUI
                                main.openUpgradeRewardGUI(player, newRarity);
                            } else {
                                if(main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.UpgradeEnchant-MaxRarity-Message-Toggle")) {
                                    // Send UpgradeMaxRarity Message in chat when called.
                                    for (String UpgradeMaxRarityMessage : main.getSettingsConfig().getStringList("UpgradeEnchantMessages.UpgradeEnchant-MaxRarity-Message")) {
                                        String withPAPISet = main.setPlaceholders(player, UpgradeMaxRarityMessage);
                                        String message = Main.color(withPAPISet);
                                        player.sendMessage(message);
                                    }
                                }
                                // Get the bool to check if the user wants to play the blacklisted enchant sound
                                boolean NotEnoughItemsPlaySound = main.getSettingsConfig().getBoolean("UpgradeEnchantSounds.UpgradeEnchant-NotEnoughItems-Sound-Toggle");
                                // check if the user wants to play the Already Applied sound
                                if(NotEnoughItemsPlaySound){
                                    // Get apply sound via config.
                                    Sound NotEnoughItemsSound = Sound.valueOf(main.getSettingsConfig().getString("UpgradeEnchantSounds.UpgradeEnchant-NotEnoughItems-Sound"));
                                    float NotEnoughItemsVolume = main.getSettingsConfig().getInt("UpgradeEnchantSounds.UpgradeEnchant-NotEnoughItems-Volume");
                                    float NotEnoughItemsPitch = main.getSettingsConfig().getInt("UpgradeEnchantSounds.UpgradeEnchant-NotEnoughItems-Pitch");

                                    // Play sound for when enchant is Already Applied.
                                    player.playSound(player.getLocation(), NotEnoughItemsSound, NotEnoughItemsVolume, NotEnoughItemsPitch);
                                }
                            }
                        }
                    } else if(matchingCount > 0) {
                        if(main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.UpgradeEnchant-NotEnoughItems-Message-Toggle")) {
                            // Send UpgradeNotEnoughItems Message in chat when called.
                            for (String UpgradeNotEnoughItemsMessage : main.getSettingsConfig().getStringList("UpgradeEnchantMessages.UpgradeEnchant-NotEnoughItems-Message")) {
                                String withPAPISet = main.setPlaceholders(player, UpgradeNotEnoughItemsMessage);
                                String message = Main.color(withPAPISet)
                                        .replace("{oldRarity}", requiredRarity);
                                player.sendMessage(message);
                            }
                        }
                        // Get the bool to check if the user wants to play the blacklisted enchant sound
                        boolean MaxRarityPlaySound = main.getSettingsConfig().getBoolean("UpgradeEnchantSounds.UpgradeEnchant-MaxRarity-Sound-Toggle");
                        // check if the user wants to play the Already Applied sound
                        if(MaxRarityPlaySound){
                            // Get apply sound via config.
                            Sound MaxRaritySound = Sound.valueOf(main.getSettingsConfig().getString("UpgradeEnchantSounds.UpgradeEnchant-MaxRarity-Sound"));
                            float MaxRarityVolume = main.getSettingsConfig().getInt("UpgradeEnchantSounds.UpgradeEnchant-MaxRarity-Volume");
                            float MaxRarityPitch = main.getSettingsConfig().getInt("UpgradeEnchantSounds.UpgradeEnchant-MaxRarity-Pitch");

                            // Play sound for when enchant is Already Applied.
                            player.playSound(player.getLocation(), MaxRaritySound, MaxRarityVolume, MaxRarityPitch);
                        }
                    }
                }
            }
        }

        // ===== Upgrade Reward Menu ===== \\
        // Get the UpgradeReward Menu title from the config
        String upgradeRewardMenuTitle = Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.UpgradeReward-Menu-Title"));
        // Check if the clicked inventory matches your custom GUI title
        if (title.equals(Main.color(upgradeRewardMenuTitle))) {
            // Check if the clicked inventory is the custom GUI, not the player's inventory
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                int slot = event.getSlot();

                // Cancel clicks on specific slots in the GUI
                if (slot >= 0 && slot <= 26 && slot != 13) {
                    event.setCancelled(true);
                }

                // Handle clicks within your custom GUI
//                if (slot == 13) {
//                    player.sendMessage(Main.color("&a&lReward Slot"));
//                }
            }
        }

        // ===== EnchantListGUI Menu ===== \\
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
                    main.openEnchantRarityListGUI(player, "GODLY");
                    raritySelected = "GODLY";
                }
                if (slot == main.getMenusConfig().getInt("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.LEGENDARY.Slot")) {
                    main.openEnchantRarityListGUI(player, "LEGENDARY");
                    raritySelected = "LEGENDARY";
                }
                if (slot == main.getMenusConfig().getInt("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.EPIC.Slot")) {
                    main.openEnchantRarityListGUI(player, "EPIC");
                    raritySelected = "EPIC";
                }
                if (slot == main.getMenusConfig().getInt("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities.RARE.Slot")) {
                    main.openEnchantRarityListGUI(player, "RARE");
                    raritySelected = "RARE";
                }
            }
        }

        // ===== EnchantRarityListGUI Menu ===== \\
        // Get the EnchantRarityListGUI Menu title from the config
//        String EnchantRarityListGUI = Main.color(main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.EnchantList-Menu-Title"));
        String EnchantRarityListGUI = Main.color(main.getMenusConfig().getString("EnchantList-Gui.EnchantList-Menu.EnchantList-Menu-Title"))
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
                }

                // Handle clicks on the enchantment items
                if (!clicked.hasItemMeta()) return;

                // Check if the sender does not have the permission and is not an operator
                if (!player.hasPermission("atlasenchants.enchantlistgrabber") && !player.isOp()) {
                    // Send noPermission Message in chat when called.
                    if(main.getMenusConfig().getBoolean("EnchantList-Gui.RarityList-Menu.RarityList-Menu-Grabber-Message"))
                    {
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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Get the closed inventory and its title
        Inventory inventory = event.getInventory();
        String inventoryTitle = event.getView().getTitle();  // This gets the displayed title

        // ===== Upgrade Enchant Menu ===== \\
        // Check if the title matches your custom GUI title
        if(!hasUpgraded) {
            if (inventoryTitle.equals(Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Title")))) {  // Replace with your actual GUI title
                Player player = (Player) event.getPlayer();

                // Define the slots where players can place their items (e.g., 11 to 24)
                int[] validSlots = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};

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

                // Optional: Inform the player that their items have been returned
//                player.sendMessage(Main.color("Your items have been returned to your inventory."));
            }
        }

        // ===== Upgrade Reward Menu ===== \\
        // Check if the title matches your custom GUI title
        if (inventoryTitle.equals(Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.UpgradeReward-Menu-Title")))) {  // Replace with your actual GUI title
            Player player = (Player) event.getPlayer();

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

            // Optional: Inform the player that their items have been returned
//            player.sendMessage(Main.color("Your items have been returned to your inventory."));
        }
    }

    private String getNextRarity(String currentRarity) {
        // Define the rarity hierarchy
        Map<String, String> rarityUpgradeMap = new HashMap<>();
        rarityUpgradeMap.put("RARE", "EPIC");
        rarityUpgradeMap.put("EPIC", "LEGENDARY");
        rarityUpgradeMap.put("LEGENDARY", "GODLY");

        // Get the next rarity from the map
        return rarityUpgradeMap.get(currentRarity);
    }

    private int getCurrentPageFromTitle(String title) {
        // Remove Minecraft color/formatting codes
        String cleanTitle = title.replaceAll("§[0-9a-fk-orA-FK-OR]", "").replaceAll("&[0-9a-fk-orA-FK-OR]", "");

        String[] parts = cleanTitle.split(" ");

        // Ensure the title has at least two parts and the last part is a number
        if (parts.length > 1 && isNumeric(parts[parts.length - 1])) {
            return Integer.parseInt(parts[parts.length - 1]) - 1;
        }

        // Default to page 0 if no valid page number is found
        return 0;
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

