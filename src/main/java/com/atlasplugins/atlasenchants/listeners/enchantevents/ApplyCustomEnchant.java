package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ApplyCustomEnchant implements Listener {

    private Main main;

    public ApplyCustomEnchant(Main main) {
        this.main = main;
    }

    private static String ConvertToRomanNumeral(int number) {
        if (number < 1 || number > 1000) {
            // In a real plugin, you'd use main.getLogger().warning() or similar for logging
            // System.out.println("Invalid number for Roman numeral conversion: " + number);
            return null;
        }

        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] units = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

        int thousandsDigit = number / 1000;
        int hundredsDigit = (number % 1000) / 100;
        int tensDigit = (number % 100) / 10;
        int unitsDigit = number % 10;

        StringBuilder romanNumeral = new StringBuilder();
        romanNumeral.append(thousands[thousandsDigit]);
        romanNumeral.append(hundreds[hundredsDigit]);
        romanNumeral.append(tens[tensDigit]);
        romanNumeral.append(units[unitsDigit]);

        return romanNumeral.toString();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent invEvent) {
        // Basic event checks: player inventory, non-null items
        if (invEvent.getClickedInventory() == null || invEvent.getClickedInventory().getType() != InventoryType.PLAYER) return;

        Player player = (Player) invEvent.getWhoClicked();
        ItemStack clickedItem = invEvent.getCurrentItem();
        ItemStack cursorItem = invEvent.getCursor();

        if (clickedItem == null || cursorItem == null) return;

        // Check if the cursor item is the specific custom enchantment item type
        Material enchantItemMaterial = Material.valueOf(main.getSettingsConfig().getString("EnchantItems.EnchantItem"));
        if (cursorItem.getType() != enchantItemMaterial) return;

        // Check for custom enchantment data on the cursor item
        if (!cursorItem.hasItemMeta()) return;
        ItemMeta cursorMeta = cursorItem.getItemMeta();
        PersistentDataContainer cursorPDC = cursorMeta.getPersistentDataContainer();

        if (!cursorPDC.has(Main.customEnchantKeys, PersistentDataType.STRING)) return;

        // Parse enchantment name and level from the cursor item's PDC
        String cursorData = cursorPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);
        if (cursorData == null) return; // Should not happen due to has() check

        String[] enchantParts = cursorData.split(":");
        if (enchantParts.length < 2) {
            main.getLogger().warning("Invalid custom enchantment data format on cursor item: " + cursorData);
            return;
        }
        String enchantName = enchantParts[0];
        int enchantLevel = Integer.parseInt(enchantParts[1]);

        // Call the refactored applyCustomEnchantment method
        ItemStack modifiedItem = applyCustomEnchantment(main, player, clickedItem, enchantName, enchantLevel);

        // If the enchantment was successfully applied (modifiedItem is not null)
        if (modifiedItem != null) {
            invEvent.setCurrentItem(modifiedItem); // Explicitly set the modified item back to the slot
            invEvent.setCursor(new ItemStack(Material.AIR)); // Consume the enchantment item
            player.updateInventory(); // Refresh player's inventory display
            invEvent.setCancelled(true); // Prevent default inventory click behavior
        }
    }

    /**
     * Applies a custom enchantment to an ItemStack. This method can be called
     * independently of an InventoryClickEvent, making it suitable for command usage,
     * other plugin integrations, or any direct enchantment application.
     * It handles validation, lore/PDC updates, sound, and message feedback.
     *
     * @param main The main plugin instance.
     * @param player The player receiving the enchantment (for feedback).
     * @param targetItem The ItemStack to which the enchantment is being applied.
     * @param enchantName The name of the custom enchantment (e.g., "FEARSIGHT").
     * @param enchantLevel The level of the custom enchantment (e.g., 1).
     * @return The modified ItemStack if the enchantment was successfully applied,
     * or null if it could not be applied due to validation rules.
     */
    public static ItemStack applyCustomEnchantment(Main main, Player player, ItemStack targetItem, String enchantName, int enchantLevel) {

        // Basic validation of inputs
        if (targetItem == null || enchantName == null || enchantName.isEmpty() || enchantLevel <= 0) {
            main.getLogger().warning("Attempted to apply enchantment with invalid parameters (null item, empty name, or invalid level).");
            return null;
        }

        // Get a mutable copy of the ItemMeta
        ItemMeta itemMeta = targetItem.getItemMeta();
        if (itemMeta == null) {
            // If the item doesn't have ItemMeta, it means it's a basic item without custom data.
            // We can create one to proceed, or return null if only items with existing meta are allowed.
            // For now, let's assume it should always have meta for custom enchants.
            return null;
        }
        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();

        // 1. Check if the enchantment is enabled
        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments." + enchantName + ".Enchantment-Enabled");
        if (!isEnchantmentEnabled) {
            handleFeedback(main, player, "EnchantItemSounds.EnchantItem-DisabledEnchant", "EnchantItemMessages.EnchantItem-DisabledEnchant",
                    "{disabledEnchantName}", formatEnchantName(enchantName), "{disabledEnchantLevel}", String.valueOf(enchantLevel));
            return null; // Enchantment is disabled
        }

        List<String> lore = itemMeta.hasLore() ? new ArrayList<>(itemMeta.getLore()) : new ArrayList<>(); // Create a mutable copy
        List<String> newEnchantmentsDataList = new ArrayList<>(); // To store enchantment data strings for PDC

        // 2. Check for existing enchantments on the target item
        if (itemPDC.has(Main.customEnchantKeys, PersistentDataType.STRING)) {
            String existingEnchantData = itemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);
            if (existingEnchantData != null && !existingEnchantData.isEmpty()) {
                String[] existingEnchantments = existingEnchantData.split(",");

                for (String existingEnchant : existingEnchantments) {
                    String[] existingEnchantParts = existingEnchant.split(":");
                    if (existingEnchantParts.length < 2) {
                        main.getLogger().warning("Malformed existing enchantment data on item: " + existingEnchant);
                        continue; // Skip malformed existing data
                    }
                    String existingEnchantName = existingEnchantParts[0];
                    int existingEnchantLevel = Integer.parseInt(existingEnchantParts[1]);

                    // 2a. Check against blacklist (if the new enchant is blacklisted by an existing one)
                    boolean blacklistSystemEnabled = main.getSettingsConfig().getBoolean("EnchantItemMessages.EnchantItem-Blacklisted-Toggle");
                    List<String> blacklist = main.getEnchantmentsConfig().getStringList("Enchantments." + enchantName + ".Enchantment-Blacklist-Enchants");

                    if (blacklistSystemEnabled && blacklist.contains(existingEnchantName)) {
                        handleFeedback(main, player, "EnchantItemSounds.EnchantItem-Blacklisted", "EnchantItemMessages.EnchantItem-Blacklisted",
                                "{enchantName}", formatEnchantName(enchantName),
                                "{enchantLevel}", String.valueOf(enchantLevel),
                                "{blackListedEnchantName}", formatEnchantName(existingEnchantName),
                                "{blackListedEnchantLevel}", String.valueOf(existingEnchantLevel));
                        return null; // Cannot apply due to blacklist conflict
                    }

                    // 2b. Check if the exact enchantment already exists at equal or higher level
                    if (existingEnchantName.equals(enchantName)) {
                        if (existingEnchantLevel >= enchantLevel) {
                            handleFeedback(main, player, "EnchantItemSounds.EnchantItem-AlreadyApplied", "EnchantItemMessages.EnchantItem-AlreadyApplied",
                                    "{enchantName}", formatEnchantName(enchantName),
                                    "{enchantLevel}", String.valueOf(enchantLevel),
                                    "{existingEnchantName}", formatEnchantName(existingEnchantName),
                                    "{existingEnchantLevel}", String.valueOf(existingEnchantLevel));
                            return null; // Enchantment already applied at sufficient level
                        } else {
                            // New enchantment is higher level, remove old lore entry
                            String fakeExistingEnchantName = formatEnchantName(existingEnchantName);
                            lore.removeIf(line -> Main.color(line).contains(fakeExistingEnchantName));
                        }
                    } else {
                        // Keep existing enchantments that are not the current one
                        newEnchantmentsDataList.add(existingEnchant);
                    }
                }
            }
        }

        // 3. Construct the data string for the new enchantment (e.g., "ENCHANT_NAME:LEVEL[:ID]")
        // If enchantID is always part of your PDC storage, ensure it's included here.
        // Assuming it's not strictly necessary for unique identification from a command/API call.
        // If it is, you'd need to decide on a default ID or pass it as a parameter.
        // For robustness, let's add a placeholder "0" if the original format expected an ID.
        String newEnchantmentPDCData = enchantName + ":" + enchantLevel + ":0"; // Added ":0" as placeholder ID

        // Add the new enchantment data to the list
        newEnchantmentsDataList.add(newEnchantmentPDCData);
        String updatedEnchantData = String.join(",", newEnchantmentsDataList);
        itemPDC.set(Main.customEnchantKeys, PersistentDataType.STRING, updatedEnchantData);

        // 4. Check if the target item type is applicable for this enchantment
        List<String> applicableItems = main.getEnchantmentsConfig().getStringList("Enchantments." + enchantName + ".Enchantment-Apply-Item");
        Material targetItemType = targetItem.getType();

        if (applicableItems.contains(targetItemType.toString())) {
            // Format for lore display
            String formattedEnchantName = formatEnchantName(enchantName);
            String romanEnchantLevel = ConvertToRomanNumeral(enchantLevel);

            // Construct and add lore entry
            String enchantLoreConfig = main.getEnchantmentsConfig().getString("Enchantments." + enchantName + ".Enchantment-Apply-Lore");
            String enchantLoreWithPAPI = main.setPlaceholders(player, enchantLoreConfig);
            String enchantLore = Main.color(enchantLoreWithPAPI)
                    .replace("{enchantmentName}", formattedEnchantName)
                    .replace("{lvl}", romanEnchantLevel);
            lore.add(enchantLore);
            itemMeta.setLore(lore);

            // Apply updated metadata to the item
            targetItem.setItemMeta(itemMeta);

            // Handle success feedback (sound and message)
            handleFeedback(main, player, "EnchantItemSounds.EnchantItem-Apply", "EnchantItemMessages.EnchantItem-Success",
                    "{enchantName}", formattedEnchantName, "{enchantLevel}", String.valueOf(enchantLevel));

            return targetItem; // Enchantment applied successfully, return the modified item
        } else {
            // Item is not applicable for this enchantment.
            // Note: PDC was already updated. If you want to revert PDC on non-applicable items,
            // you'd need to manage a rollback or apply PDC only after this check.
//            main.getLogger().info(player.getName() + " tried to apply " + enchantName + " to an inapplicable item: " + targetItemType.toString());
            // You might want a specific feedback message/sound here for non-applicable items.
            return null; // Item type is not valid for this enchantment
        }
    }

    /**
     * Helper method to handle consistent sound and message feedback based on configuration.
     *
     * @param main The main plugin instance.
     * @param player The player to send feedback to.
     * @param soundConfigPath The base path for sound settings in config (e.g., "EnchantItemSounds.EnchantItem-Apply").
     * @param messageConfigPath The base path for message settings in config (e.g., "EnchantItemMessages.EnchantItem-Success").
     * @param placeholders Key-value pairs of placeholders to replace in messages (e.g., "{enchantName}", "MyEnchant").
     */
    private static void handleFeedback(Main main, Player player, String soundConfigPath, String messageConfigPath, String... placeholders) {
        // Sound handling
        boolean playSound = main.getSettingsConfig().getBoolean(soundConfigPath + "-Sound-Toggle");
        if (playSound) {
            try {
                Sound sound = Sound.valueOf(main.getSettingsConfig().getString(soundConfigPath + "-Sound"));
                float volume = (float) main.getSettingsConfig().getDouble(soundConfigPath + "-Volume");
                float pitch = (float) main.getSettingsConfig().getDouble(soundConfigPath + "-Pitch");
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                main.getLogger().warning("Invalid sound specified in config path: " + soundConfigPath + "-Sound. Error: " + e.getMessage());
            }
        }

        // Message handling
        boolean sendMessage = main.getSettingsConfig().getBoolean(messageConfigPath + "-Message-Toggle");
        if (sendMessage) {
            List<String> messages = main.getSettingsConfig().getStringList(messageConfigPath + "-Message");
            for (String msg : messages) {
                String processedMsg = main.setPlaceholders(player, msg);
                for (int i = 0; i < placeholders.length; i += 2) {
                    if (i + 1 < placeholders.length) {
                        processedMsg = processedMsg.replace(placeholders[i], placeholders[i + 1]);
                    }
                }
                player.sendMessage(Main.color(processedMsg));
            }
        }
    }

    private static String formatEnchantName(String enchantName) {
        String formattedName = enchantName.replace('-', ' ');
        String[] words = formattedName.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}