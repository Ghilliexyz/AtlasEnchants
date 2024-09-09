package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class GuiListener implements Listener {

    private Main main;
    private boolean hasUpgraded = false;

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

        // ===== Upgrade Enchant Menu ===== \\
        // Get the UpgradeEnchant Menu title from the config
        String upgradeEnchantMenuTitle = Main.color(main.getSettingsConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Title"));
        // Check if the clicked inventory matches your custom GUI title
        if (title.equals(Main.color(upgradeEnchantMenuTitle))) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                int slot = event.getSlot();

                // Cancel clicks on specific slots in the GUI
                if (slot == 0 || slot == 1 || slot == 2 || slot == 3 || slot == 4 || slot == 5 || slot == 6 || slot == 7 || slot == 8 || slot == 9 || slot == 10
                        || slot == 16 || slot == 17 || slot == 18 || slot == 19 || slot == 25 || slot == 26 || slot == 27 || slot == 28 || slot == 29 || slot == 30
                        || slot == 31 || slot == 32 || slot == 33 || slot == 34 || slot == 35 || slot == 36 || slot == 37 || slot == 38 || slot == 39 || slot == 40
                        || slot == 41 || slot == 42 || slot == 43 || slot == 44 || slot == 45 || slot == 46 || slot == 47 || slot == 48 || slot == 49 || slot == 50
                        || slot == 51 || slot == 52 || slot == 53) {
                    event.setCancelled(true); // Cancel the event to prevent item taking/moving in the GUI
                }

                // Handle clicks within your custom GUI
//                if (slot == 11) {
//                    player.sendMessage(Main.color("enchant slot 1"));
//                }
//                if (slot == 12) {
//                    player.sendMessage(Main.color("enchant slot 2"));
//                }
//                if (slot == 13) {
//                    player.sendMessage(Main.color("enchant slot 3"));
//                }
//                if (slot == 14) {
//                    player.sendMessage(Main.color("enchant slot 4"));
//                }
//                if (slot == 15) {
//                    player.sendMessage(Main.color("enchant slot 5"));
//                }
//                if (slot == 20) {
//                    player.sendMessage(Main.color("enchant slot 6"));
//                }
//                if (slot == 21) {
//                    player.sendMessage(Main.color("enchant slot 7"));
//                }
//                if (slot == 22) {
//                    player.sendMessage(Main.color("enchant slot 8"));
//                }
//                if (slot == 23) {
//                    player.sendMessage(Main.color("enchant slot 9"));
//                }
//                if (slot == 24) {
//                    player.sendMessage(Main.color("enchant slot 10"));
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
                    } else {
                        if(main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.UpgradeEnchant-NotEnoughItems-Message-Toggle")) {
                            // Send UpgradeNotEnoughItems Message in chat when called.
                            for (String UpgradeNotEnoughItemsMessage : main.getSettingsConfig().getStringList("UpgradeEnchantMessages.UpgradeEnchant-NotEnoughItems-Message")) {
                                String withPAPISet = main.setPlaceholders(player, UpgradeNotEnoughItemsMessage);
                                assert requiredRarity != null;
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
        String upgradeRewardMenuTitle = Main.color(main.getSettingsConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.UpgradeReward-Menu-Title"));
        // Check if the clicked inventory matches your custom GUI title
        if (title.equals(Main.color(upgradeRewardMenuTitle))) {
            // Check if the clicked inventory is the custom GUI, not the player's inventory
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                int slot = event.getSlot();

                // Cancel clicks on specific slots in the GUI
                if (slot == 0 || slot == 1 || slot == 2 || slot == 3 || slot == 4 || slot == 5 || slot == 6 || slot == 7 || slot == 8 || slot == 9 || slot == 10
                        || slot == 11 || slot == 12 || slot == 14 || slot == 15 || slot == 16 || slot == 17 || slot == 18 || slot == 19 || slot == 20 || slot == 21
                        || slot == 22 || slot == 23 || slot == 24 || slot == 25 || slot == 26) {
                    event.setCancelled(true); // Cancel the event to prevent item taking/moving in the GUI
                }

                // Handle clicks within your custom GUI
//                if (slot == 13) {
//                    player.sendMessage(Main.color("&a&lReward Slot"));
//                }
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
            if (inventoryTitle.equals(Main.color(main.getSettingsConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Title")))) {  // Replace with your actual GUI title
                Player player = (Player) event.getPlayer();

                // Define the slots where players can place their items (e.g., 11 to 24)
                int[] validSlots = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24};

                // Loop through the defined valid slots and return the items to the player
                for (int slot : validSlots) {
                    ItemStack item = inventory.getItem(slot);

                    if (item != null && item.getType() != org.bukkit.Material.AIR) {
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
        if (inventoryTitle.equals(Main.color(main.getSettingsConfig().getString("UpgradeEnchant-Gui.UpgradeReward-Menu.UpgradeReward-Menu-Title")))) {  // Replace with your actual GUI title
            Player player = (Player) event.getPlayer();

            // Define the slots where players can place their items (e.g., 13)
            int[] validSlots = {13};

            // Loop through the defined valid slots and return the items to the player
            for (int slot : validSlots) {
                ItemStack item = inventory.getItem(slot);

                if (item != null && item.getType() != org.bukkit.Material.AIR) {
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

