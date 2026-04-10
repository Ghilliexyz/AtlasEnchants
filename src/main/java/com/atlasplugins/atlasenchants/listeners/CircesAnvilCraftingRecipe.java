package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCircesAnvil;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCircesEmber;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateScrapOfCirce;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.HashSet;
import java.util.Set;

public class CircesAnvilCraftingRecipe {

    private Main main;

    public CircesAnvilCraftingRecipe(Main main) {
        this.main = main;
    }

    public void registerCircesAnvilRecipe() {
        CreateCircesAnvil createCircesAnvil = new CreateCircesAnvil(main);
        ItemStack anvilItem = createCircesAnvil.CreateCircesAnvilItem(1, null);

        CreateCircesEmber createCircesEmber = new CreateCircesEmber(main);
        CreateScrapOfCirce createScrapOfCirce = new CreateScrapOfCirce(main);

        NamespacedKey recipeKey = new NamespacedKey(main, "circes_anvil_recipe");
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, anvilItem);

        String row1 = main.getEnchantmentsConfig().getString("CircesAnvil.CircesAnvil-Crafting-Row-1");
        String row2 = main.getEnchantmentsConfig().getString("CircesAnvil.CircesAnvil-Crafting-Row-2");
        String row3 = main.getEnchantmentsConfig().getString("CircesAnvil.CircesAnvil-Crafting-Row-3");

        Set<Character> usedChars = new HashSet<>();
        for (char c : (row1 + row2 + row3).toCharArray()) {
            if (c != ' ') usedChars.add(c);
        }

        recipe.shape(row1, row2, row3);

        for (char c : usedChars) {
            if (c == 'Y') {
                recipe.setIngredient('Y', new RecipeChoice.ExactChoice(createScrapOfCirce.CreateScrapOfCirceItem(1, null)));
                continue;
            }

            if (c == 'Z') {
                recipe.setIngredient('Z', new RecipeChoice.ExactChoice(createCircesEmber.CreateCircesEmberItem(1, null)));
                continue;
            }

            String configPath = "CircesAnvil.CircesAnvil-Crafting-Materials-" + c;
            if (!main.getEnchantmentsConfig().contains(configPath)) {
                continue;
            }

            String matName = main.getEnchantmentsConfig().getString(configPath);
            Material material = Material.matchMaterial(matName);
            if (material == null) {
                continue;
            }

            recipe.setIngredient(c, material);
        }

        Bukkit.addRecipe(recipe);
    }
}
