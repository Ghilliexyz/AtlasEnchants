package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
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
        boolean hasDisplaced = false;
        String who = resolveWho(event);
        List<String> enchantments = main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false)
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

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

        // One weighted roll picks the rarity, then a random enabled enchant of that rarity is handed out.
        ConfigurationSection oddsSection = main.getSettingsConfig().getConfigurationSection("EnchantItems.EnchantItem-Rarity-List");
        LinkedHashMap<String, Double> weights = RarityRoller.eligibleWeights(main, oddsSection, enchantments);
        if (weights.isEmpty()) {
            main.debugOddsInfo("Loot", who, "No eligible rarities to roll - check the rarity weights and that enchants are enabled.");
            return;
        }

        String chosenRarity = RarityRoller.pickWeightedRarity(random, weights);
        if (chosenRarity == null) return;

        List<String> filteredEnchantments = RarityRoller.enabledEnchantsOfRarity(main, chosenRarity, enchantments);
        if (filteredEnchantments.isEmpty()) return;

        String selectedEnchantment = filteredEnchantments.get(random.nextInt(filteredEnchantments.size()));
        int enchantmentMaxLevel = main.getEnchantmentsConfig().getInt("Enchantments." + selectedEnchantment + ".Enchantment-MaxLvl");
        if (enchantmentMaxLevel <= 0) return;

        int enchantmentLevel = random.nextInt(enchantmentMaxLevel) + 1;

        main.debugOddsInfo("Loot", who, "Granted " + selectedEnchantment + " (Lvl " + enchantmentLevel + ") from rarity "
                + chosenRarity + " - picked from " + filteredEnchantments.size() + " enchant(s).");

        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
        ItemStack customItem = createCustomEnchant.CreateCustomEnchantmentItem(selectedEnchantment, enchantmentLevel, 1, null);
        hasDisplaced = addLoot(event, customItem, hasDisplaced);
    }
}
