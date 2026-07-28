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
        Material enchantItemMaterial = Main.getMaterial(main.getSettingsConfig().getString("EnchantItems.EnchantItem"), Material.ENCHANTED_BOOK);
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
            invEvent.setCancelled(true); // Prevent default inventory click behavior first
            invEvent.getClickedInventory().setItem(invEvent.getSlot(), modifiedItem); // Directly set item in slot

            // Consume only ONE book from the cursor stack. Clearing the whole cursor destroyed
            // every other book in the stack while only ever applying one enchant.
            if (cursorItem.getAmount() > 1) {
                cursorItem.setAmount(cursorItem.getAmount() - 1);
                player.setItemOnCursor(cursorItem);
            } else {
                player.setItemOnCursor(new ItemStack(Material.AIR));
            }

            player.updateInventory(); // Refresh player's inventory display
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
            handleFeedback(main, player, "EnchantItemSounds.DisabledEnchant", "EnchantItemMessages.DisabledEnchant",
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
                    boolean blacklistSystemEnabled = main.getSettingsConfig().getBoolean("EnchantItemMessages.Blacklisted.Enabled");
                    List<String> blacklist = main.getEnchantmentsConfig().getStringList("Enchantments." + enchantName + ".Enchantment-Blacklist-Enchants");

                    if (blacklistSystemEnabled && blacklist.contains(existingEnchantName)) {
                        handleFeedback(main, player, "EnchantItemSounds.Blacklisted", "EnchantItemMessages.Blacklisted",
                                "{enchantName}", formatEnchantName(enchantName),
                                "{enchantLevel}", String.valueOf(enchantLevel),
                                "{blackListedEnchantName}", formatEnchantName(existingEnchantName),
                                "{blackListedEnchantLevel}", String.valueOf(existingEnchantLevel));
                        return null; // Cannot apply due to blacklist conflict
                    }

                    // 2b. Check if the exact enchantment already exists at equal or higher level
                    if (existingEnchantName.equals(enchantName)) {
                        if (existingEnchantLevel >= enchantLevel) {
                            handleFeedback(main, player, "EnchantItemSounds.AlreadyApplied", "EnchantItemMessages.AlreadyApplied",
                                    "{enchantName}", formatEnchantName(enchantName),
                                    "{enchantLevel}", String.valueOf(enchantLevel),
                                    "{existingEnchantName}", formatEnchantName(existingEnchantName),
                                    "{existingEnchantLevel}", String.valueOf(existingEnchantLevel));
                            return null; // Enchantment already applied at sufficient level
                        } else {
                            // New enchantment is higher level, remove the old lore entry
                            removeAppliedLore(main, player, lore, existingEnchantName, existingEnchantLevel);
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

            // Construct and add lore entry
            String enchantLore = buildAppliedLore(main, player, enchantName, enchantLevel);
            if (enchantLore != null) {
                lore.add(enchantLore);
            }
            itemMeta.setLore(lore);

            // Apply updated metadata to the item
            targetItem.setItemMeta(itemMeta);

            // Handle success feedback (sound and message)
            handleFeedback(main, player, "EnchantItemSounds.Apply", "EnchantItemMessages.Success",
                    "{enchantName}", formattedEnchantName, "{enchantLevel}", String.valueOf(enchantLevel));

            return targetItem; // Enchantment applied successfully, return the modified item
        } else {
            // Item is not applicable for this enchantment. Nothing is written to the item itself -
            // the PDC edit above went to a detached ItemMeta copy that is simply discarded here.
            //
            // Tell the player why, otherwise dropping a book on the wrong gear looks like the
            // plugin is broken: nothing happens and no message is given.
            handleFeedback(main, player, "EnchantItemSounds.NotApplicable", "EnchantItemMessages.NotApplicable",
                    "{enchantName}", formatEnchantName(enchantName),
                    "{enchantLevel}", String.valueOf(enchantLevel),
                    "{itemName}", formatEnchantName(targetItemType.toString().replace('_', '-')),
                    "{applicableItems}", formatApplicableItems(applicableItems));
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
        boolean playSound = main.getSettingsConfig().getBoolean(soundConfigPath + ".Toggle");
        if (playSound) {
            try {
                Sound sound = Main.getSound(main.getSettingsConfig().getString(soundConfigPath + ".Sound"));
                float volume = (float) main.getSettingsConfig().getDouble(soundConfigPath + ".Volume");
                float pitch = (float) main.getSettingsConfig().getDouble(soundConfigPath + ".Pitch");
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                main.getLogger().warning("Invalid sound specified in config path: " + soundConfigPath + ".Sound. Error: " + e.getMessage());
            }
        }

        // Message handling
        boolean sendMessage = main.getSettingsConfig().getBoolean(messageConfigPath + ".Toggle");
        if (sendMessage) {
            List<String> messages = main.getSettingsConfig().getStringList(messageConfigPath + ".Message");
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

    /** The literal placeholder the Apply-Lore template uses for the enchant level. */
    private static final String LEVEL_PLACEHOLDER = "{lvl}";

    /**
     * Builds the single lore line this plugin writes onto an item when an enchant is applied,
     * from that enchant's {@code Enchantment-Apply-Lore} template.
     *
     * @return the finished line, or {@code null} when the enchant has no Apply-Lore configured.
     */
    private static String buildAppliedLore(Main main, Player player, String enchantName, int enchantLevel) {
        String template = renderAppliedLoreTemplate(main, player, enchantName);
        if (template == null) return null;

        String romanEnchantLevel = ConvertToRomanNumeral(enchantLevel);
        return template.replace(LEVEL_PLACEHOLDER, romanEnchantLevel == null ? "" : romanEnchantLevel);
    }

    /** The Apply-Lore template with everything resolved except {lvl}, which is left in place. */
    private static String renderAppliedLoreTemplate(Main main, Player player, String enchantName) {
        String template = main.getEnchantmentsConfig().getString("Enchantments." + enchantName + ".Enchantment-Apply-Lore");
        if (template == null) return null;

        return Main.color(main.setPlaceholders(player, template))
                .replace("{enchantmentName}", formatEnchantName(enchantName));
    }

    /**
     * Removes the lore line this plugin wrote for {@code enchantName} at {@code existingLevel},
     * so an upgrade doesn't leave "Leech I" sitting above "Leech III".
     *
     * <p>Matches the whole rendered Apply-Lore line rather than just the enchant's name. The name
     * on its own also matched lore the <em>player</em> wrote - renaming a sword's lore to something
     * like "Forged in the Leech Caverns" meant upgrading Leech deleted that line too.
     */
    private static void removeAppliedLore(Main main, Player player, List<String> lore, String enchantName, int existingLevel) {
        String template = renderAppliedLoreTemplate(main, player, enchantName);
        if (template == null) return;

        // The line as it was written at the old level - the normal case.
        String exact = buildAppliedLore(main, player, enchantName, existingLevel);
        if (exact != null && lore.removeIf(line -> line.equals(exact))) return;

        // Nothing matched, so the item predates a change to the Apply-Lore format or to the level
        // numbering. Fall back to the line's shape: same text either side of where the level sits,
        // whatever the level itself now reads as.
        int marker = template.indexOf(LEVEL_PLACEHOLDER);
        if (marker < 0) {
            lore.removeIf(line -> line.equals(template));
            return;
        }

        String prefix = template.substring(0, marker);
        String suffix = template.substring(marker + LEVEL_PLACEHOLDER.length());

        // A template that is nothing but "{lvl}" would match every line on the item, so leave it.
        if (prefix.isEmpty() && suffix.isEmpty()) return;

        lore.removeIf(line -> line.length() >= prefix.length() + suffix.length()
                && line.startsWith(prefix)
                && line.endsWith(suffix));
    }

    /**
     * Renders the Enchantment-Apply-Item list for a message, e.g. "Diamond Pickaxe, Netherite
     * Pickaxe". Long lists are truncated so the message does not flood chat.
     */
    private static String formatApplicableItems(List<String> applicableItems) {
        if (applicableItems == null || applicableItems.isEmpty()) return "nothing";

        int shown = Math.min(applicableItems.size(), 8);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < shown; i++) {
            if (i > 0) result.append(", ");
            result.append(formatEnchantName(applicableItems.get(i).replace('_', '-')));
        }
        if (applicableItems.size() > shown) {
            result.append(" (+").append(applicableItems.size() - shown).append(" more)");
        }
        return result.toString();
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