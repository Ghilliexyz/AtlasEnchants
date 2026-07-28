package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.configuration.ConfigurationSection;
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

    /**
     * Rolls a random custom enchant book against the spawn rarity weights in settings.yml.
     */
    public ItemStack CreateRandomCustomEnchantmentItem(Player p, int enchantmentAmount, boolean givePlayerEnchant, String desiredRarity) {
        ConfigurationSection odds = main.getSettingsConfig().getConfigurationSection("EnchantItems.EnchantItem-Rarity-List");
        return rollBatch(p, enchantmentAmount, givePlayerEnchant, desiredRarity, odds, "Spawn");
    }

    /**
     * Rolls a random custom enchant book against the Altar Of Circe rarity weights in enchantments.yml.
     */
    public ItemStack CreateRandomOracleEnchantmentItem(Player p, int enchantmentAmount, boolean givePlayerEnchant, String desiredRarity) {
        ConfigurationSection odds = main.getEnchantmentsConfig().getConfigurationSection("AltarOfCirce.AltarOfCirce-Book-Enchanter-Odds");
        return rollBatch(p, enchantmentAmount, givePlayerEnchant, desiredRarity, odds, "Altar");
    }

    /**
     * Rolls {@code enchantmentAmount} books, each one an independent roll.
     *
     * <p>The amount used to be applied by handing the <em>same</em> book over repeatedly, so
     * "give 5 random enchants" produced 5 copies of one enchant rather than 5 rolls.
     *
     * @return the last book rolled, or {@code null} when nothing could be rolled.
     */
    private ItemStack rollBatch(Player p, int enchantmentAmount, boolean givePlayerEnchant,
                                String desiredRarity, ConfigurationSection oddsSection, String source) {
        // Callers that only want an item back (loot chests, trader stock) never ask for more than one.
        if (!givePlayerEnchant || p == null) {
            return rollAndCreate(p, givePlayerEnchant, desiredRarity, oddsSection, source);
        }

        ItemStack last = null;
        for (int i = 0; i < Math.max(1, enchantmentAmount); i++) {
            ItemStack rolled = rollAndCreate(p, true, desiredRarity, oddsSection, source);
            if (rolled != null) last = rolled;
        }
        return last;
    }

    /**
     * Picks a rarity with a single weighted roll (or honours {@code desiredRarity} when the caller
     * forces one), then hands out a random enabled enchant of that rarity at a random level.
     *
     * @param oddsSection the rarity -> weight section to roll against.
     * @param source      label used in the debug odds readout ("Spawn"/"Altar").
     * @return the created book, or {@code null} when nothing could be rolled.
     */
    private ItemStack rollAndCreate(Player p, boolean givePlayerEnchant,
                                    String desiredRarity, ConfigurationSection oddsSection, String source) {
        String who = p != null ? p.getName() : "Unknown";

        List<String> allEnchantments = main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false)
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        String chosenRarity;
        if (desiredRarity != null) {
            // The caller wants a specific rarity (e.g. an upgrade reward), so skip the weighted roll.
            chosenRarity = desiredRarity.toUpperCase();
        } else {
            LinkedHashMap<String, Double> weights = RarityRoller.eligibleWeights(main, oddsSection, allEnchantments);
            if (weights.isEmpty()) {
                main.debugOddsInfo(source, who, "No eligible rarities to roll - check the odds weights and that enchants are enabled.");
                return null;
            }
            chosenRarity = RarityRoller.pickWeightedRarity(random, weights);
            if (chosenRarity == null) return null;
        }

        List<String> filteredEnchantments = RarityRoller.enabledEnchantsOfRarity(main, chosenRarity, allEnchantments);
        if (filteredEnchantments.isEmpty()) {
            main.debugOddsInfo(source, who, "No enabled enchants for rarity " + chosenRarity + ".");
            return null;
        }

        String selected = filteredEnchantments.get(random.nextInt(filteredEnchantments.size()));
        int maxLevel = main.getEnchantmentsConfig().getInt("Enchantments." + selected + ".Enchantment-MaxLvl");
        if (maxLevel <= 0) {
            main.debugOddsInfo(source, who, "Skipping " + selected + " - invalid Enchantment-MaxLvl " + maxLevel + ".");
            return null;
        }

        int level = random.nextInt(maxLevel) + 1;

        main.debugOddsInfo(source, who, "Granted " + selected + " (Lvl " + level + ") from rarity " + chosenRarity
                + " - picked from " + filteredEnchantments.size() + " enchant(s).");

        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);

        // Building and handing over in one call keeps each book's enchant ID unique. Adding the
        // same built item repeatedly made the books stack, and applying a stack destroyed all of it.
        if (givePlayerEnchant && p != null) {
            return createCustomEnchant.CreateCustomEnchantmentItem(selected, level, 1, p);
        }

        return createCustomEnchant.CreateCustomEnchantmentItem(selected, level, 1, null);
    }
}
