package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class CreateRandomCustomEnchant implements Listener {

    private Main main;
    private final Random random = new Random();

    public CreateRandomCustomEnchant(Main main) {
        this.main = main;
    }

    // New parameter for specifying rarity
    public ItemStack CreateRandomCustomEnchantmentItem(Player p, int enchantmentAmount, boolean givePlayerEnchant, String desiredRarity) {
        boolean hasFoundEnchantment = false;
        List<String> enchantments = main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false)
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        boolean flipEnchantmentList = main.getSettingsConfig().getBoolean("EnchantItems.EnchantItem-Flip-List");

        List<String> enchantmentRarity = main.getSettingsConfig().getConfigurationSection("EnchantItems.EnchantItem-Rarity-List").getKeys(false)
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        if (flipEnchantmentList) {
            Collections.reverse(enchantments);
            Collections.reverse(enchantmentRarity);
        }

        // If a specific rarity is provided, use it, otherwise, continue with random selection
        while (!hasFoundEnchantment) {
            for (String rarity : enchantmentRarity) {
                // If a specific rarity is chosen, skip this loop unless it's the desired rarity
                if (desiredRarity != null && !desiredRarity.equalsIgnoreCase(rarity)) {
                    continue;
                }

                double rarityChance = main.getSettingsConfig().getDouble("EnchantItems.EnchantItem-Rarity-List." + rarity);

                // Only check rarity chance if no specific rarity is given
                if (desiredRarity == null && random.nextDouble() > rarityChance) {
                    continue;
                }

                String enchantRarity = rarity.toUpperCase();

                // Filter enchantments by the current rarity
                List<String> filteredEnchantments = enchantments.stream()
                        .filter(enchantment -> {
                            String spawnEnchantRarity = main.getEnchantmentsConfig().getString("Enchantments." + enchantment + ".Enchantment-Rarity");
                            return enchantRarity.equalsIgnoreCase(spawnEnchantRarity);
                        })
                        .collect(Collectors.toList());

                if (!filteredEnchantments.isEmpty()) {
                    // Select a random enchantment from the filtered list
                    String selectedEnchantment = filteredEnchantments.get(random.nextInt(filteredEnchantments.size()));

                    // Get Enchantment Enabled Status
                    boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments." + selectedEnchantment + ".Enchantment-Enabled");
                    if (!isEnchantmentEnabled) continue;

                    // Get the Enchantment Max Level
                    int enchantmentMaxLevel = main.getEnchantmentsConfig().getInt("Enchantments." + selectedEnchantment + ".Enchantment-MaxLvl");

                    // Generate a random level between 1 (inclusive) and enchantmentMaxLevel (inclusive)
                    int enchantmentLevel = random.nextInt(enchantmentMaxLevel) + 1;

                    // Create the custom enchantment item
                    CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
                    ItemStack customItem = createCustomEnchant.CreateCustomEnchantmentItem(selectedEnchantment, enchantmentLevel, enchantmentAmount, null);

                    if (givePlayerEnchant && p != null) {
                        for (int i = 0; i < enchantmentAmount; i++) {
                            // Add the item to the player's inventory or drop it if the inventory is full
                            HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(customItem);
                            if (!remainingItems.isEmpty()) {
                                for (ItemStack item : remainingItems.values()) {
                                    p.getWorld().dropItemNaturally(p.getLocation(), item);
                                }
                            }
                        }
                    }

                    hasFoundEnchantment = true;
                    return customItem;
                }

                // Exit the loop if a specific rarity was used
                if (desiredRarity != null) {
                    break;
                }
            }
        }
        return null;
    }

    // New parameter for specifying rarity
    public ItemStack CreateRandomOracleEnchantmentItem(Player p, int enchantmentAmount, boolean givePlayerEnchant, String desiredRarity) {
        boolean hasFoundEnchantment = false;

        List<String> allEnchantments = main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false)
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        boolean flipEnchantmentList = main.getEnchantmentsConfig().getBoolean("OraclesTable.OraclesTable-Flip-List");

        List<String> allRarities = main.getEnchantmentsConfig().getConfigurationSection("OraclesTable.OraclesTable-Book-Enchanter-Odds").getKeys(false)
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        if (flipEnchantmentList) {
            Collections.reverse(allEnchantments);
            Collections.reverse(allRarities);
        }

        while (!hasFoundEnchantment) {
            for (String rarity : allRarities) {
                if (desiredRarity != null && !desiredRarity.equalsIgnoreCase(rarity)) continue;

                double rarityChance = main.getEnchantmentsConfig().getDouble("OraclesTable.OraclesTable-Book-Enchanter-Odds." + rarity);

                if (desiredRarity == null && random.nextDouble() > rarityChance) continue;

                String upperRarity = rarity.toUpperCase();
                List<String> filteredEnchantments = getEnabledEnchantmentsByRarity(upperRarity, allEnchantments);

                if (filteredEnchantments.isEmpty()) {
                    if (desiredRarity != null) return null; // None available for that rarity
                    continue;
                }

                // Pick a random enchantment
                String selected = filteredEnchantments.get(random.nextInt(filteredEnchantments.size()));
                int maxLevel = main.getEnchantmentsConfig().getInt("Enchantments." + selected + ".Enchantment-MaxLvl");

                // Prevent invalid max level
                if (maxLevel <= 0) continue;

                int level = random.nextInt(maxLevel) + 1;

                CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
                ItemStack customItem = createCustomEnchant.CreateCustomEnchantmentItem(selected, level, enchantmentAmount, null);

                if (givePlayerEnchant && p != null) {
                    for (int i = 0; i < enchantmentAmount; i++) {
                        HashMap<Integer, ItemStack> remaining = p.getInventory().addItem(customItem);
                        if (!remaining.isEmpty()) {
                            for (ItemStack leftover : remaining.values()) {
                                p.getWorld().dropItemNaturally(p.getLocation(), leftover);
                            }
                        }
                    }
                }

                hasFoundEnchantment = true;
                return customItem;
            }
        }

        return null; // Shouldn't reach here unless something went wrong
    }

    private List<String> getEnabledEnchantmentsByRarity(String rarity, List<String> allEnchantments) {
        return allEnchantments.stream()
                .filter(enchant -> {
                    String enchantRarity = main.getEnchantmentsConfig().getString("Enchantments." + enchant + ".Enchantment-Rarity");
                    boolean isEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments." + enchant + ".Enchantment-Enabled");
                    return isEnabled && rarity.equalsIgnoreCase(enchantRarity);
                })
                .collect(Collectors.toList());
    }
}
