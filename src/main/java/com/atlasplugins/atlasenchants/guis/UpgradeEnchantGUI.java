package com.atlasplugins.atlasenchants.guis;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UpgradeEnchantGUI extends Gui {

    private Main main;
    private Player player;

    private boolean hasUpgraded = false;

    public UpgradeEnchantGUI(Main main, Player player) {
        // Directly pass the fetched values to super()
        super(player,
                Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Title")), 54);

        this.player = player;

        // Continue with the rest of your constructor logic
        this.main = main;
        setupItems();
    }

    @Override
    public void setupItems() {
        // ---------- Upgrade Enchant Btn ---------- \\
            // Create Item \\
            String UpgradeAcceptableBtnTitle = main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-Acceptable-Title");
            Material UpgradeAcceptableBtnConfigItem = Material.valueOf(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-Acceptable-Item"));
            ItemStack UpgradeAcceptableBtnItem = new ItemStack(UpgradeAcceptableBtnConfigItem);
            ItemMeta UpgradeAcceptableBtnItemMeta = UpgradeAcceptableBtnItem.getItemMeta();
            // Set Title \\
            String UpgradeAcceptableBtnItemDisplayName = Main.color(UpgradeAcceptableBtnTitle).replace("{Player}", player.getName());
            String UpgradeAcceptableBtnItemDisplayNamePAPISet = main.setPlaceholders(player, UpgradeAcceptableBtnItemDisplayName);
            assert UpgradeAcceptableBtnItemMeta != null;
            // Set Lore \\
            ArrayList<String> UpgradeAcceptableBtnLore = new ArrayList<>();
            for (String WorldInfo : main.getMenusConfig().getStringList("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-Acceptable-Lore")) {
                String withPAPISet = main.setPlaceholders(player, WorldInfo);
                UpgradeAcceptableBtnLore.add(Main.color(withPAPISet));
            }
            // Set all values \\
            UpgradeAcceptableBtnItemMeta.setLore(UpgradeAcceptableBtnLore);
            UpgradeAcceptableBtnItemMeta.setDisplayName(Main.color(UpgradeAcceptableBtnItemDisplayNamePAPISet));
            UpgradeAcceptableBtnItem.setItemMeta(UpgradeAcceptableBtnItemMeta);
            // Place Items in correct slots \\
            inventory.setItem(40, UpgradeAcceptableBtnItem);

                //// ELSE STATEMENT
//            // Create Item \\
//            String UpgradeNotAcceptableBtnTitle = main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-NotAcceptable-Title");
//            Material UpgradeNotAcceptableBtnConfigItem = Material.valueOf(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-NotAcceptable-Item"));
//            ItemStack UpgradeNotAcceptableBtnItem = new ItemStack(UpgradeNotAcceptableBtnConfigItem);
//            ItemMeta UpgradeNotAcceptableBtnItemMeta = UpgradeNotAcceptableBtnItem.getItemMeta();
//            // Set Title \\
//            String UpgradeNotAcceptableBtnItemDisplayName = Main.color(UpgradeNotAcceptableBtnTitle).replace("{Player}", player.getName());
//            String UpgradeNotAcceptableBtnItemDisplayNamePAPISet = main.setPlaceholders(player, UpgradeNotAcceptableBtnItemDisplayName);
//            assert UpgradeNotAcceptableBtnItemMeta != null;
//            // Set Lore \\
//            ArrayList<String> UpgradeNotAcceptableBtnLore = new ArrayList<>();
//            for (String WorldInfo : main.getMenusConfig().getStringList("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Btn.UpgradeEnchant-Menu-Upgrade-Btn-NotAcceptable-Lore")) {
//                String withPAPISet = main.setPlaceholders(player, WorldInfo);
//                UpgradeNotAcceptableBtnLore.add(Main.color(withPAPISet));
//            }
//            // Set all values \\
//            UpgradeNotAcceptableBtnItemMeta.setLore(UpgradeNotAcceptableBtnLore);
//            UpgradeNotAcceptableBtnItemMeta.setDisplayName(Main.color(UpgradeNotAcceptableBtnItemDisplayNamePAPISet));
//            UpgradeNotAcceptableBtnItem.setItemMeta(UpgradeNotAcceptableBtnItemMeta);
//            // Place Items in correct slots \\
//            inventory.setItem(40, UpgradeNotAcceptableBtnItem);

        // ---------- GLASS FILLER ---------- \\
        // Create Item \\
        String GlassTitle = main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Filler-Title");
        Material GlassConfigItem = Material.valueOf(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Filler-Item"));
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
        inventory.setItem(16, GlassItem);
        inventory.setItem(17, GlassItem);
        inventory.setItem(18, GlassItem);
        inventory.setItem(19, GlassItem);
        inventory.setItem(25, GlassItem);
        inventory.setItem(26, GlassItem);
        inventory.setItem(27, GlassItem);
        inventory.setItem(28, GlassItem);
        inventory.setItem(29, GlassItem);
        inventory.setItem(30, GlassItem);
        inventory.setItem(31, GlassItem);
        inventory.setItem(32, GlassItem);
        inventory.setItem(33, GlassItem);
        inventory.setItem(34, GlassItem);
        inventory.setItem(35, GlassItem);
        inventory.setItem(36, GlassItem);
        inventory.setItem(37, GlassItem);
        inventory.setItem(38, GlassItem);
        inventory.setItem(39, GlassItem);
        inventory.setItem(41, GlassItem);
        inventory.setItem(42, GlassItem);
        inventory.setItem(43, GlassItem);
        inventory.setItem(44, GlassItem);
        inventory.setItem(45, GlassItem);
        inventory.setItem(46, GlassItem);
        inventory.setItem(47, GlassItem);
        inventory.setItem(48, GlassItem);
        inventory.setItem(49, GlassItem);
        inventory.setItem(50, GlassItem);
        inventory.setItem(51, GlassItem);
        inventory.setItem(52, GlassItem);
        inventory.setItem(53, GlassItem);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        hasUpgraded = false;
        // Get the inventory title
        String title = event.getView().getTitle();
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
                            //                            main.getLogger().info("next rarity: " + newRarity);
                            if (newRarity != null) {
                                hasUpgraded = true;
                                if (main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.UpgradeEnchant-SuccessUpgrade-Message-Toggle")) {
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
                                if (SuccessUpgradePlaySound) {
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
                                if (main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.UpgradeEnchant-MaxRarity-Message-Toggle")) {
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
                                if (NotEnoughItemsPlaySound) {
                                    // Get apply sound via config.
                                    Sound NotEnoughItemsSound = Sound.valueOf(main.getSettingsConfig().getString("UpgradeEnchantSounds.UpgradeEnchant-NotEnoughItems-Sound"));
                                    float NotEnoughItemsVolume = main.getSettingsConfig().getInt("UpgradeEnchantSounds.UpgradeEnchant-NotEnoughItems-Volume");
                                    float NotEnoughItemsPitch = main.getSettingsConfig().getInt("UpgradeEnchantSounds.UpgradeEnchant-NotEnoughItems-Pitch");

                                    // Play sound for when enchant is Already Applied.
                                    player.playSound(player.getLocation(), NotEnoughItemsSound, NotEnoughItemsVolume, NotEnoughItemsPitch);
                                }
                            }
                        }
                    } else if (matchingCount > 0) {
                        if (main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.UpgradeEnchant-NotEnoughItems-Message-Toggle")) {
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
                        if (MaxRarityPlaySound) {
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
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // Get the inventory title
        String title = event.getView().getTitle();
        // Check if the title matches your custom GUI title
        if(!hasUpgraded) {
            if (title.equals(Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Title")))) {  // Replace with your actual GUI title

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
            }
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
}
