package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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

    // Who/what triggered this loot generation, for the debug readout: the opening player
    // if there is one, otherwise the context entity, otherwise the world-gen location.
    private String resolveWho(LootGenerateEvent e) {
        if (e.getEntity() instanceof Player player) return player.getName();
        if (e.getEntity() != null) return e.getEntity().getType().toString();
        Location loc = e.getLootContext() != null ? e.getLootContext().getLocation() : null;
        if (loc != null) {
            return String.format("world-gen @ %d,%d,%d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        return "unknown";
    }

    // Small containers (decorated pots hold 1 slot, dispensers 9, ...) will make Spigot log
    // "Tried to over-fill a container" if we append more loot than the destination can hold.
    private boolean hasRoom(LootGenerateEvent e) {
        if (e.getInventoryHolder() == null) return true; // entity/fishing loot, no fixed capacity
        return e.getLoot().size() < e.getInventoryHolder().getInventory().getSize();
    }

    // Adds our item to the loot. When the destination is already full we displace a vanilla
    // entry instead of dropping the item, but only once per generation - otherwise a later
    // roll would overwrite the item an earlier roll just placed.
    private boolean addLoot(LootGenerateEvent e, ItemStack item, boolean hasDisplaced) {
        if (hasRoom(e)) {
            e.getLoot().add(item);
            return hasDisplaced;
        }
        if (hasDisplaced || e.getLoot().isEmpty()) return hasDisplaced;
        e.getLoot().set(random.nextInt(e.getLoot().size()), item);
        return true;
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        // Enchantment Spawner
//        main.getLogger().info("--------------------------------------------------");
        boolean hasFoundEnchantment = false;
        boolean hasDisplaced = false;
        String who = resolveWho(event);
        List<String> enchantments = main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false)
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        boolean flipEnchantmentList = main.getSettingsConfig().getBoolean("EnchantItems.EnchantItem-Flip-List");

        double chanceToSpawnEnchants = main.getSettingsConfig().getDouble("EnchantItems.EnchantItem-Spawn-Chance");

        // --- Oblivion Shard spawner (rolls independently of the enchant-book chance) ---
        boolean isShardEnabled = main.getEnchantmentsConfig().getBoolean("OblivionShard.OblivionShard-Enabled");
        if (isShardEnabled) {
            double shardSpawnChance = main.getEnchantmentsConfig().getDouble("OblivionShard.OblivionShard-Spawn-Chance");
            double shardRoll = random.nextDouble();
            boolean shardPassed = shardRoll < shardSpawnChance;
            main.debugOddsRoll("Loot", who, "Oblivion Shard", shardRoll, shardSpawnChance, shardPassed);
            if (shardPassed) {
                CreateShard createShard = new CreateShard(main);
                hasDisplaced = addLoot(event, createShard.CreateShardItem(1, null), hasDisplaced);
            }
        }

        // --- Circe's Ember spawner (rolls independently of the enchant-book chance) ---
        boolean isEmberEnabled = main.getEnchantmentsConfig().getBoolean("CircesEmber.CircesEmber-Enabled", true);
        if (isEmberEnabled) {
            double emberSpawnChance = main.getEnchantmentsConfig().getDouble("CircesEmber.CircesEmber-Spawn-Chance", 0.03);
            double emberRoll = random.nextDouble();
            boolean emberPassed = emberRoll < emberSpawnChance;
            main.debugOddsRoll("Loot", who, "Circe's Ember", emberRoll, emberSpawnChance, emberPassed);
            if (emberPassed) {
                CreateCircesEmber createCircesEmber = new CreateCircesEmber(main);
                hasDisplaced = addLoot(event, createCircesEmber.CreateCircesEmberItem(1, null), hasDisplaced);
            }
        }

        // --- Circe's Brand spawner (rolls independently of the enchant-book chance) ---
        boolean isBrandEnabled = main.getEnchantmentsConfig().getBoolean("CircesBrand.CircesBrand-Enabled", true);
        if (isBrandEnabled) {
            double brandSpawnChance = main.getEnchantmentsConfig().getDouble("CircesBrand.CircesBrand-Spawn-Chance", 0.01);
            double brandRoll = random.nextDouble();
            boolean brandPassed = brandRoll < brandSpawnChance;
            main.debugOddsRoll("Loot", who, "Circe's Brand", brandRoll, brandSpawnChance, brandPassed);
            if (brandPassed) {
                CreateCircesBrand createCircesBrand = new CreateCircesBrand(main);
                hasDisplaced = addLoot(event, createCircesBrand.CreateCircesBrandItem(1, null), hasDisplaced);
            }
        }

        // return if the enchant-book spawn chance has failed (books only; shard/ember already rolled above).
        double enchantBookRoll = random.nextDouble();
        boolean enchantBookPassed = enchantBookRoll <= chanceToSpawnEnchants;
        main.debugOddsRoll("Loot", who, "Enchant Book", enchantBookRoll, chanceToSpawnEnchants, enchantBookPassed);
        if (!enchantBookPassed) return;
        if (!hasRoom(event) && hasDisplaced) return;

        List<String> enchantmentRarity = main.getSettingsConfig().getConfigurationSection("EnchantItems.EnchantItem-Rarity-List").getKeys(false)
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        if (flipEnchantmentList) {
            Collections.reverse(enchantments);
            Collections.reverse(enchantmentRarity);
        }

        int maxAttempts = 100;
        while (!hasFoundEnchantment && maxAttempts-- > 0) {
            for (String rarity : enchantmentRarity) {
                double rarityChance = main.getSettingsConfig().getDouble("EnchantItems.EnchantItem-Rarity-List." + rarity);
                double rarityRoll = random.nextDouble();
                boolean rarityPassed = rarityRoll <= rarityChance;
                main.debugOddsRoll("Loot", who, "Rarity " + rarity, rarityRoll, rarityChance, rarityPassed);
                if (rarityPassed) {
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

//                        main.getLogger().info("Selected Enchantment: " + selectedEnchantment);

                        // Get Enchantment Enabled Status
                        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments." + selectedEnchantment + ".Enchantment-Enabled");
                        // if Enchantment Enabled = false skip.
                        if (!isEnchantmentEnabled) continue;

                        // Get the Enchantment Max Level
                        int enchantmentMaxLevel = main.getEnchantmentsConfig().getInt("Enchantments." + selectedEnchantment + ".Enchantment-MaxLvl"); // Example enchantment level
                        int enchantmentAmount = 1;

                        // Generate a random number between 1 (inclusive) and enchantmentMaxLevel (inclusive)
                        int enchantmentLevel = random.nextInt(enchantmentMaxLevel) + 1;

                        // Create an instance of CreateCustomEnchant and call the method
                        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
                        ItemStack customItem = createCustomEnchant.CreateCustomEnchantmentItem(selectedEnchantment, enchantmentLevel, enchantmentAmount, null);

//                    main.getLogger().info("SPAWN ENCHANT: " + selectedEnchantment);
                        hasDisplaced = addLoot(event, customItem, hasDisplaced);
                        hasFoundEnchantment = true;
                        break; // Exit after adding one enchantment
                    }
                }
            }
        }
    }
}
