package com.atlasplugins.atlasenchants.Listeners;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

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
        List<String> enchantments = main.getConfig().getConfigurationSection("Enchantments").getKeys(false).stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        double chanceToSpawnEnchants = main.getConfig().getDouble("EnchantItems.EnchantItem-Spawn-Chance");

        if (random.nextDouble() < chanceToSpawnEnchants)
        {
            int enchantmentListAmount = enchantments.size();
            int count = 0;

            for (String enchantment : enchantments)
            {
                // Get the Enchantment Max Level
                int enchantmentMaxLevel = main.getConfig().getInt("Enchantments." + enchantment + ".Enchantment-MaxLvl"); // Example enchantment level
                int enchantmentAmount = 1; // Example number of items to generate

                // Generate a random number between 1 (inclusive) and enchantmentMaxLevel (inclusive)
                int enchantmentLevel = random.nextInt(enchantmentMaxLevel) + 1;

                // Create an instance of CreateCustomEnchant and call the method
                CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
                ItemStack customItem = createCustomEnchant.CreateCustomEnchantmentItem(enchantment, enchantmentLevel, enchantmentAmount, null);

                // Get Enchantment Spawn Chance
                double enchantItemChance = main.getConfig().getDouble("Enchantments." + enchantment + ".Enchantment-Spawn-Chance");

                // Increase Count
                count++;

                // Break the loop once it has looped once
                if(count >= enchantmentListAmount) {
                    break;
                }

                // Check if the item should be added based on chance
                if (random.nextDouble() < enchantItemChance) {
                    event.getLoot().add(customItem);
                    break;
                }
            }
        }
    }
}
