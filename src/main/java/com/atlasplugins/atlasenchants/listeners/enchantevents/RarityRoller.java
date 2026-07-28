package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Weighted rarity selection shared by every place that rolls a random custom enchant
 * (loot chests, the enchant-book spawner, and the Altar Of Circe).
 *
 * <p>Each rarity owns a slice of a single number line sized by its configured weight. One roll
 * lands in exactly one slice, so the numbers in config are the real relative odds: bigger weight
 * = bigger slice = more likely. The selection is order-independent (no Flip-List needed) and needs
 * no retry loop, since one roll always resolves to exactly one eligible rarity.
 *
 * <p>A weight of {@code 0} gives a rarity a zero-width slice, so it is genuinely impossible to
 * roll - the previous {@code <= chance} edge case that could leak a 0-chance rarity is gone.
 */
public final class RarityRoller {

    private RarityRoller() {}

    /**
     * @return the enabled enchant names whose configured rarity matches {@code rarity}.
     */
    public static List<String> enabledEnchantsOfRarity(Main main, String rarity, List<String> allEnchantments) {
        return allEnchantments.stream()
                .filter(enchant -> {
                    String enchantRarity = main.getEnchantmentsConfig().getString("Enchantments." + enchant + ".Enchantment-Rarity");
                    boolean isEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments." + enchant + ".Enchantment-Enabled");
                    return isEnabled && rarity.equalsIgnoreCase(enchantRarity);
                })
                .collect(Collectors.toList());
    }

    /**
     * Builds the rarity -> weight map to roll against from an odds config section. A rarity is
     * included only when its weight is above 0 <em>and</em> at least one enabled enchant carries
     * that rarity - so the roll can never pick a rarity that has nothing to hand out.
     */
    public static LinkedHashMap<String, Double> eligibleWeights(Main main, ConfigurationSection oddsSection, List<String> allEnchantments) {
        LinkedHashMap<String, Double> weights = new LinkedHashMap<>();
        if (oddsSection == null) return weights;

        for (String rarity : oddsSection.getKeys(false)) {
            String upper = rarity.toUpperCase();
            double weight = oddsSection.getDouble(rarity);
            if (weight <= 0) continue;
            if (enabledEnchantsOfRarity(main, upper, allEnchantments).isEmpty()) continue;
            weights.put(upper, weight);
        }
        return weights;
    }

    /**
     * Rolls once and returns the rarity whose slice the roll landed in, chosen with probability
     * proportional to its weight.
     *
     * @return the chosen rarity, or {@code null} when there is nothing weighted to pick from.
     */
    public static String pickWeightedRarity(Random random, Map<String, Double> weightedRarities) {
        double total = 0;
        for (double weight : weightedRarities.values()) {
            if (weight > 0) total += weight;
        }
        if (total <= 0) return null;

        double roll = random.nextDouble() * total;
        double cumulative = 0;
        for (Map.Entry<String, Double> entry : weightedRarities.entrySet()) {
            if (entry.getValue() <= 0) continue;
            cumulative += entry.getValue();
            if (roll < cumulative) return entry.getKey();
        }
        return null; // Only reachable through floating-point rounding; treated as no selection.
    }
}
