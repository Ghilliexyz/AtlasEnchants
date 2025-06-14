package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateOracleBook;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateOracleTable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

public class OraclesTableCraftingRecipe {

    private Main main;

    public OraclesTableCraftingRecipe(Main main) {
        this.main = main;
    }

    public void registerOracleTableRecipe()
    {
        // Create result item
        CreateOracleTable createOracleTable = new CreateOracleTable(main);
        ItemStack oracleItem = createOracleTable.CreateOracleTableItem(1, null);
        // Create Oracle Book
        CreateOracleBook createOracle = new CreateOracleBook(main);

        ShapedRecipe recipe = new ShapedRecipe(Main.customOracleTableKeys, oracleItem);

        recipe.shape("ENE", "PBP", "SSS");

        recipe.setIngredient('E', Material.END_CRYSTAL);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('S', Material.ECHO_SHARD);
        recipe.setIngredient('P', Material.PHANTOM_MEMBRANE);
        recipe.setIngredient('B', new RecipeChoice.ExactChoice(createOracle.CreateOracleItem(1, null)) {
        });

        Bukkit.addRecipe(recipe);
    }
}
