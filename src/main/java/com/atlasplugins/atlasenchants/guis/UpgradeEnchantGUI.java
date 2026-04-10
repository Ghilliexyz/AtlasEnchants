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
    private final int requiredBooks;
    private final int[] validSlots;

    // All possible book slots: row 1 (11-15) and row 2 (20-24)
    private static final int[] ROW1_SLOTS = {11, 12, 13, 14, 15};
    private static final int[] ROW2_SLOTS = {20, 21, 22, 23, 24};

    public UpgradeEnchantGUI(Main main, Player player) {
        // Directly pass the fetched values to super()
        super(player,
                Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.Title")), 54);

        this.player = player;

        // Continue with the rest of your constructor logic
        this.main = main;
        this.requiredBooks = Math.max(1, Math.min(10, main.getSettingsConfig().getInt("UpgradeEnchantSettings.Required-Books", 10)));
        this.validSlots = computeValidSlots(this.requiredBooks);
    }

    @Override
    public void setupItems() {
        // ---------- GLASS FILLER ---------- \\
        String GlassTitle = main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.Filler-Title");
        Material GlassConfigItem = Material.valueOf(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.Filler-Item"));
        ItemStack GlassItem = new ItemStack(GlassConfigItem);
        ItemMeta GlassItemMeta = GlassItem.getItemMeta();
        String GlassItemDisplayName = Main.color(GlassTitle).replace("{Player}", player.getName());
        String GlassItemDisplayNamePAPISet = main.setPlaceholders(player, GlassItemDisplayName);
        if (GlassItemMeta == null) return;
        GlassItemMeta.setDisplayName(Main.color(GlassItemDisplayNamePAPISet));
        GlassItem.setItemMeta(GlassItemMeta);
        // Fill all slots with glass \\
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, GlassItem);
        }

        // ---------- Upgrade Enchant Btn ---------- \\
        String UpgradeAcceptableBtnTitle = main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.Btn.Acceptable.Title");
        Material UpgradeAcceptableBtnConfigItem = Material.valueOf(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.Btn.Acceptable.Item"));
        ItemStack UpgradeAcceptableBtnItem = new ItemStack(UpgradeAcceptableBtnConfigItem);
        ItemMeta UpgradeAcceptableBtnItemMeta = UpgradeAcceptableBtnItem.getItemMeta();
        String UpgradeAcceptableBtnItemDisplayName = Main.color(UpgradeAcceptableBtnTitle).replace("{Player}", player.getName());
        String UpgradeAcceptableBtnItemDisplayNamePAPISet = main.setPlaceholders(player, UpgradeAcceptableBtnItemDisplayName);
        if (UpgradeAcceptableBtnItemMeta == null) return;
        ArrayList<String> UpgradeAcceptableBtnLore = new ArrayList<>();
        for (String WorldInfo : main.getMenusConfig().getStringList("UpgradeEnchant-Gui.UpgradeEnchant-Menu.Btn.Acceptable.Lore")) {
            String withPAPISet = main.setPlaceholders(player, WorldInfo);
            UpgradeAcceptableBtnLore.add(Main.color(withPAPISet));
        }
        UpgradeAcceptableBtnItemMeta.setLore(UpgradeAcceptableBtnLore);
        UpgradeAcceptableBtnItemMeta.setDisplayName(Main.color(UpgradeAcceptableBtnItemDisplayNamePAPISet));
        UpgradeAcceptableBtnItem.setItemMeta(UpgradeAcceptableBtnItemMeta);
        inventory.setItem(40, UpgradeAcceptableBtnItem);

        // ---------- Clear valid book slots based on Required-Books config ---------- \\
        for (int slot : validSlots) {
            inventory.setItem(slot, null);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        hasUpgraded = false;
        // Get the inventory title
        String title = event.getView().getTitle();
        // Get the UpgradeEnchant Menu title from the config
        String upgradeEnchantMenuTitle = Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.Title"));
        // Check if the clicked inventory matches your custom GUI title
        if (title.equals(Main.color(upgradeEnchantMenuTitle))) {
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                int slot = event.getSlot();

                // Cancel clicks on non-book slots
                boolean isBookSlot = false;
                for (int s : validSlots) {
                    if (slot == s) {
                        isBookSlot = true;
                        break;
                    }
                }
                if (!isBookSlot) {
                    event.setCancelled(true);
                }

                // Check if the upgrade button is clicked (assume slot 40 is the upgrade button)
                if (slot == 40) {
                    Inventory inventory = event.getInventory();
                    String requiredRarity = null;
                    int matchingCount = 0;

                    // Loop through valid book slots
                    for (int i : validSlots) {
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

                    // Check if there are enough matching enchantments
                    if (matchingCount >= requiredBooks) {
                        // Upgrade logic
                        if (requiredRarity != null) {
                            String newRarity = getNextRarity(requiredRarity);
                            //                            main.getLogger().info("next rarity: " + newRarity);
                            if (newRarity != null) {
                                hasUpgraded = true;
                                if (main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.SuccessUpgrade.Toggle")) {
                                    // Send UpgradeMaxRarity Message in chat when called.
                                    for (String UpgradeMaxRarityMessage : main.getSettingsConfig().getStringList("UpgradeEnchantMessages.SuccessUpgrade.Message")) {
                                        String withPAPISet = main.setPlaceholders(player, UpgradeMaxRarityMessage);
                                        String message = Main.color(withPAPISet)
                                                .replace("{oldRarity}", requiredRarity)
                                                .replace("{newRarity}", newRarity)
                                                .replace("{requiredBooks}", String.valueOf(requiredBooks));
                                        player.sendMessage(message);
                                    }
                                }
                                // Get the bool to check if the user wants to play the blacklisted enchant sound
                                boolean SuccessUpgradePlaySound = main.getSettingsConfig().getBoolean("UpgradeEnchantSounds.SuccessUpgrade.Toggle");
                                // check if the user wants to play the Already Applied sound
                                if (SuccessUpgradePlaySound) {
                                    // Get apply sound via config.
                                    Sound SuccessUpgradeSound = Sound.valueOf(main.getSettingsConfig().getString("UpgradeEnchantSounds.SuccessUpgrade.Sound"));
                                    float SuccessUpgradeVolume = (float) main.getSettingsConfig().getDouble("UpgradeEnchantSounds.SuccessUpgrade.Volume");
                                    float SuccessUpgradePitch = (float) main.getSettingsConfig().getDouble("UpgradeEnchantSounds.SuccessUpgrade.Pitch");

                                    // Play sound for when enchant is Already Applied.
                                    player.playSound(player.getLocation(), SuccessUpgradeSound, SuccessUpgradeVolume, SuccessUpgradePitch);
                                }
                                // open the reward GUI
                                main.openUpgradeRewardGUI(player, newRarity);
                            } else {
                                if (main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.MaxRarity.Toggle")) {
                                    // Send UpgradeMaxRarity Message in chat when called.
                                    for (String UpgradeMaxRarityMessage : main.getSettingsConfig().getStringList("UpgradeEnchantMessages.MaxRarity.Message")) {
                                        String withPAPISet = main.setPlaceholders(player, UpgradeMaxRarityMessage);
                                        String message = Main.color(withPAPISet);
                                        player.sendMessage(message);
                                    }
                                }
                                // Get the bool to check if the user wants to play the blacklisted enchant sound
                                boolean NotEnoughItemsPlaySound = main.getSettingsConfig().getBoolean("UpgradeEnchantSounds.NotEnoughItems.Toggle");
                                // check if the user wants to play the Already Applied sound
                                if (NotEnoughItemsPlaySound) {
                                    // Get apply sound via config.
                                    Sound NotEnoughItemsSound = Sound.valueOf(main.getSettingsConfig().getString("UpgradeEnchantSounds.NotEnoughItems.Sound"));
                                    float NotEnoughItemsVolume = (float) main.getSettingsConfig().getDouble("UpgradeEnchantSounds.NotEnoughItems.Volume");
                                    float NotEnoughItemsPitch = (float) main.getSettingsConfig().getDouble("UpgradeEnchantSounds.NotEnoughItems.Pitch");

                                    // Play sound for when enchant is Already Applied.
                                    player.playSound(player.getLocation(), NotEnoughItemsSound, NotEnoughItemsVolume, NotEnoughItemsPitch);
                                }
                            }
                        }
                    } else if (matchingCount > 0) {
                        if (main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.NotEnoughItems.Toggle")) {
                            // Send UpgradeNotEnoughItems Message in chat when called.
                            for (String UpgradeNotEnoughItemsMessage : main.getSettingsConfig().getStringList("UpgradeEnchantMessages.NotEnoughItems.Message")) {
                                String withPAPISet = main.setPlaceholders(player, UpgradeNotEnoughItemsMessage);
                                String message = Main.color(withPAPISet)
                                        .replace("{oldRarity}", requiredRarity)
                                        .replace("{requiredBooks}", String.valueOf(requiredBooks));
                                player.sendMessage(message);
                            }
                        }
                        // Get the bool to check if the user wants to play the blacklisted enchant sound
                        boolean MaxRarityPlaySound = main.getSettingsConfig().getBoolean("UpgradeEnchantSounds.MaxRarity.Toggle");
                        // check if the user wants to play the Already Applied sound
                        if (MaxRarityPlaySound) {
                            // Get apply sound via config.
                            Sound MaxRaritySound = Sound.valueOf(main.getSettingsConfig().getString("UpgradeEnchantSounds.MaxRarity.Sound"));
                            float MaxRarityVolume = (float) main.getSettingsConfig().getDouble("UpgradeEnchantSounds.MaxRarity.Volume");
                            float MaxRarityPitch = (float) main.getSettingsConfig().getDouble("UpgradeEnchantSounds.MaxRarity.Pitch");

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
            if (title.equals(Main.color(main.getMenusConfig().getString("UpgradeEnchant-Gui.UpgradeEnchant-Menu.Title")))) {  // Replace with your actual GUI title

                // Define the slots where players can place their items (e.g., 11 to 24)
                // Loop through the valid book slots and return the items to the player
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

    private static int[] computeValidSlots(int count) {
        if (count <= 5) {
            // Center slots in row 1
            int start = (5 - count) / 2;
            int[] slots = new int[count];
            for (int i = 0; i < count; i++) {
                slots[i] = ROW1_SLOTS[start + i];
            }
            return slots;
        } else {
            // Full first row + centered remainder in second row
            int remaining = count - 5;
            int start = (5 - remaining) / 2;
            int[] slots = new int[count];
            System.arraycopy(ROW1_SLOTS, 0, slots, 0, 5);
            for (int i = 0; i < remaining; i++) {
                slots[5 + i] = ROW2_SLOTS[start + i];
            }
            return slots;
        }
    }
}
