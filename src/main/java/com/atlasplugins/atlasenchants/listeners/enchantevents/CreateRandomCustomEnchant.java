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

    public ItemStack CreateRandomCustomEnchantmentItem(Player p, int enchantmentAmount, boolean givePlayerEnchant){
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

        while (!hasFoundEnchantment) {
            for (String rarity : enchantmentRarity) {
                double rarityChance = main.getSettingsConfig().getDouble("EnchantItems.EnchantItem-Rarity-List." + rarity);
                if (random.nextDouble() <= rarityChance) {
                    String enchantRarity = rarity.toUpperCase();

//                    main.getLogger().info("Enchant Rarity: " + enchantRarity);
//                    main.getLogger().info("Rarity Chance: " + rarityChance);

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

//                        main.getLogger().info("Selected Enchantment: " + selectedEnchantment);

                        // Get Enchantment Enabled Status
                        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments." + selectedEnchantment + ".Enchantment-Enabled");
                        // if Enchantment Enabled = false skip.
                        if (!isEnchantmentEnabled) continue;

                        // Get the Enchantment Max Level
                        int enchantmentMaxLevel = main.getEnchantmentsConfig().getInt("Enchantments." + selectedEnchantment + ".Enchantment-MaxLvl"); // Example enchantment level
//                            int enchantmentAmount = 1; // Example number of items to generate

                        // Generate a random number between 1 (inclusive) and enchantmentMaxLevel (inclusive)
                        int enchantmentLevel = random.nextInt(enchantmentMaxLevel) + 1;

                        // Create an instance of CreateCustomEnchant and call the method
                        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
                        ItemStack customItem = createCustomEnchant.CreateCustomEnchantmentItem(selectedEnchantment, enchantmentLevel, enchantmentAmount, null);

                        if(givePlayerEnchant) {
                            // Add items to player's inventory if player is not null
                            if (p != null) {
                                for (int i = 0; i < enchantmentAmount; i++) {
                                    // Check if there's space in the player's inventory
                                    HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(customItem);

                                    // If the inventory is full and the item could not be added, drop it at the player's feet
                                    if (!remainingItems.isEmpty()) {
                                        for (ItemStack item : remainingItems.values()) {
                                            p.getWorld().dropItemNaturally(p.getLocation(), item);
                                        }
                                    }
                                }
                            }
                        }
//                    main.getLogger().info("SPAWN ENCHANT: " + selectedEnchantment);
                        hasFoundEnchantment = true;
                        return customItem; // Exit after adding one enchantment
                    }
                }
            }
        }
        return null;
    }
}
