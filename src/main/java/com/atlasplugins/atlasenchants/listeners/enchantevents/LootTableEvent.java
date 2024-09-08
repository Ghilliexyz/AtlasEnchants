package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class LootTableEvent implements Listener {

    private Main main;

    public LootTableEvent(Main main) {
        this.main = main;
    }

    private final Random random = new Random();

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        // Enchantment Spawner
//        main.getLogger().info("--------------------------------------------------");
        boolean hasFoundEnchantment = false;
        List<String> enchantments = main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false)
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        boolean flipEnchantmentList = main.getSettingsConfig().getBoolean("EnchantItems.EnchantItem-Flip-List");

        double chanceToSpawnEnchants = main.getSettingsConfig().getDouble("EnchantItems.EnchantItem-Spawn-Chance");

        // return if chance to spawn has failed.
        if (random.nextDouble() > chanceToSpawnEnchants) return;

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
                        int enchantmentAmount = 1; // Example number of items to generate

                        // Generate a random number between 1 (inclusive) and enchantmentMaxLevel (inclusive)
                        int enchantmentLevel = random.nextInt(enchantmentMaxLevel) + 1;

                        // Create an instance of CreateCustomEnchant and call the method
                        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
                        ItemStack customItem = createCustomEnchant.CreateCustomEnchantmentItem(selectedEnchantment, enchantmentLevel, enchantmentAmount, null);

//                    main.getLogger().info("SPAWN ENCHANT: " + selectedEnchantment);
                        event.getLoot().add(customItem);
                        hasFoundEnchantment = true;
                        break; // Exit after adding one enchantment
                    }
                }
            }
        }

        // Oblivion Shard Spawner
        boolean isShardEnabled = main.getSettingsConfig().getBoolean("OblivionShard.OblivionShard-Enabled");

        if(isShardEnabled) {
            double shardSpawnChance = main.getSettingsConfig().getDouble("OblivionShard.OblivionShard-Spawn-Chance");

            // if the shard chance passes then well done!
            if(random.nextDouble() < shardSpawnChance) {
                // Create an instance of CreateShard and call the method
                CreateShard createShard = new CreateShard(main);
                ItemStack customShardItem = createShard.CreateShardItem(1, null);

                event.getLoot().add(customShardItem);
                return;
            }
        }
    }
}
