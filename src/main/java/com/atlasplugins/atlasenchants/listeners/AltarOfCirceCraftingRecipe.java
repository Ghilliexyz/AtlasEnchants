package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateOracleBook;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateAltarOfCirce;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateScrapOfCirce;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashSet;
import java.util.Set;

public class AltarOfCirceCraftingRecipe {

    private Main main;

    public AltarOfCirceCraftingRecipe(Main main) {
        this.main = main;
    }

    public void registerOracleTableRecipe()
    {
        // Create result item
        CreateAltarOfCirce createAltarOfCirce = new CreateAltarOfCirce(main);
        ItemStack oracleItem = createAltarOfCirce.CreateAltarOfCirceItem(1, null);
        // Create Oracle Book
        CreateOracleBook createOracle = new CreateOracleBook(main);
        // Create Scrap of Circes Weave
        CreateScrapOfCirce createScrapOfCirce = new CreateScrapOfCirce(main);

        ShapedRecipe recipe = new ShapedRecipe(Main.customAltarOfCirceKeys, oracleItem);

        String row1 = main.getEnchantmentsConfig().getString("AltarOfCirce.AltarOfCirce-Crafting-Row-1");
        String row2 = main.getEnchantmentsConfig().getString("AltarOfCirce.AltarOfCirce-Crafting-Row-2");
        String row3 = main.getEnchantmentsConfig().getString("AltarOfCirce.AltarOfCirce-Crafting-Row-3");

        // Collect all used characters from the shape
        Set<Character> usedChars = new HashSet<>();
        for (char c : (row1 + row2 + row3).toCharArray()) {
            if (c != ' ') usedChars.add(c); // ignore blank spaces
        }

        // Set shape
        recipe.shape(row1, row2, row3);

        // Loop through used characters and assign ingredients
        for (char c : usedChars) {
            if (c == 'X') {
                // Special case for Oracle Book
                recipe.setIngredient('X', new RecipeChoice.ExactChoice(createOracle.CreateOracleItem(1, null)));
                continue;
            }

            if (c == 'Y') {
                // Special case for Oracle Book
                recipe.setIngredient('Y', new RecipeChoice.ExactChoice(createScrapOfCirce.CreateScrapOfCirceItem(1, null)));
                continue;
            }

            String configPath = "AltarOfCirce.AltarOfCirce-Crafting-Materials-" + c;
            if (!main.getEnchantmentsConfig().contains(configPath)) {
//                Bukkit.getLogger().warning("[AtlasEnchants] Missing material config for '" + c + "'");
                continue;
            }

            String matName = main.getEnchantmentsConfig().getString(configPath);
            Material material = Material.matchMaterial(matName);
            if (material == null) {
//                Bukkit.getLogger().warning("[AtlasEnchants] Invalid material '" + matName + "' for '" + c + "'");
                continue;
            }

            recipe.setIngredient(c, material);
        }

        Bukkit.addRecipe(recipe);
    }
}
