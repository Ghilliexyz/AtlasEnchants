package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class WanderingTraderEvent implements Listener {

    private Main main;

    public WanderingTraderEvent(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onWanderingTraderSpawn(CreatureSpawnEvent e)
    {
        if (!(e.getEntity() instanceof WanderingTrader trader)) return;

        // For the debug readout: no player triggers a trader spawn, so report the trader's location.
        Location loc = trader.getLocation();
        String who = String.format("Trader @ %d,%d,%d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        // Both custom trades are appended to the trader's existing recipe list rather than
        // overwriting a fixed slot, so the Oracle and the Grimoire never clobber each other
        // (or the trader's vanilla trades).
        List<MerchantRecipe> recipes = new ArrayList<>(trader.getRecipes());

        MerchantRecipe oracle = buildOracleTrade(who);
        if (oracle != null) recipes.add(oracle);

        MerchantRecipe grimoire = buildRecipeBookTrade(who);
        if (grimoire != null) recipes.add(grimoire);

        MerchantRecipe brand = buildCircesBrandTrade(who);
        if (brand != null) recipes.add(brand);

        trader.setRecipes(recipes);
    }

    /** The Circe's Brand trade, or null if disabled or the spawn roll fails. */
    private MerchantRecipe buildCircesBrandTrade(String who) {
        boolean isEnabled = main.getEnchantmentsConfig().getBoolean("CircesBrand.CircesBrand-Trader-Enabled", true);
        if (!isEnabled) return null;

        double chanceOfSpawning = main.getEnchantmentsConfig().getDouble("CircesBrand.CircesBrand-Trader-Spawn-Chance", 0.15);
        double roll = Math.random();
        boolean passed = roll < chanceOfSpawning;
        main.debugOddsRoll("Trader", who, "Circe's Brand", roll, chanceOfSpawning, passed);
        if (!passed) return null;

        ItemStack cost = costItem(
                "CircesBrand.CircesBrand-Trader-Cost-Item",
                "CircesBrand.CircesBrand-Trader-Cost-Amount");

        CreateCircesBrand createCircesBrand = new CreateCircesBrand(main);
        ItemStack brandItem = createCircesBrand.CreateCircesBrandItem(1, null);

        MerchantRecipe recipe = new MerchantRecipe(brandItem, 1);
        recipe.addIngredient(cost);
        return recipe;
    }

    /** The Oracle of Enchantment trade, or null if disabled or the spawn roll fails. */
    private MerchantRecipe buildOracleTrade(String who) {
        boolean isTraderEnabled = main.getEnchantmentsConfig().getBoolean("OraclesOfEnchantment.OraclesOfEnchantment-Trader-Enabled");
        if (!isTraderEnabled) return null;

        double chanceOfSpawning = main.getEnchantmentsConfig().getDouble("OraclesOfEnchantment.OraclesOfEnchantment-Trader-Spawn-Chance");
        double roll = Math.random();
        boolean passed = roll < chanceOfSpawning;
        main.debugOddsRoll("Trader", who, "Oracle of Enchantment", roll, chanceOfSpawning, passed);
        if (!passed) return null;

        ItemStack cost = costItem(
                "OraclesOfEnchantment.OraclesOfEnchantment-Trader-Cost-Item",
                "OraclesOfEnchantment.OraclesOfEnchantment-Trader-Cost-Amount");

        CreateOracleBook createOracle = new CreateOracleBook(main);
        ItemStack oracleItem = createOracle.CreateOracleItem(1, null);

        MerchantRecipe recipe = new MerchantRecipe(oracleItem, 1);
        recipe.addIngredient(cost);
        return recipe;
    }

    /** The Circe's Grimoire (recipe book) trade, or null if disabled or the spawn roll fails. */
    private MerchantRecipe buildRecipeBookTrade(String who) {
        boolean isEnabled = main.getEnchantmentsConfig().getBoolean("CircesGrimoire.CircesGrimoire-Trader-Enabled", true);
        if (!isEnabled) return null;

        double chanceOfSpawning = main.getEnchantmentsConfig().getDouble("CircesGrimoire.CircesGrimoire-Trader-Spawn-Chance", 0.5);
        double roll = Math.random();
        boolean passed = roll < chanceOfSpawning;
        main.debugOddsRoll("Trader", who, "Circe's Grimoire", roll, chanceOfSpawning, passed);
        if (!passed) return null;

        ItemStack cost = costItem(
                "CircesGrimoire.CircesGrimoire-Trader-Cost-Item",
                "CircesGrimoire.CircesGrimoire-Trader-Cost-Amount");

        CreateRecipeBook createRecipeBook = new CreateRecipeBook(main);
        ItemStack bookItem = createRecipeBook.createRecipeBook(1, null);

        MerchantRecipe recipe = new MerchantRecipe(bookItem, 1);
        recipe.addIngredient(cost);
        return recipe;
    }

    /** Build the cost ItemStack from config, falling back to EMERALD if the material is missing or invalid. */
    private ItemStack costItem(String itemKey, String amountKey) {
        String costItemName = main.getEnchantmentsConfig().getString(itemKey, "EMERALD");
        Material itemMaterial;
        try {
            itemMaterial = Material.valueOf(costItemName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            itemMaterial = Material.EMERALD;
        }
        int itemAmount = Math.max(1, main.getEnchantmentsConfig().getInt(amountKey, 1));
        return new ItemStack(itemMaterial, itemAmount);
    }
}
