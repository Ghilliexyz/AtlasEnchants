package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.concurrent.ThreadLocalRandom;

public class WanderingTraderEvent implements Listener {

    private Main main;

    public WanderingTraderEvent(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onWanderingTraderSpawn(CreatureSpawnEvent e)
    {
        double chanceOfSpawning = main.getEnchantmentsConfig().getDouble("OraclesOfEnchantment.OraclesOfEnchantment-Trader-Spawn-Chance");

        boolean isTraderEnabled = main.getEnchantmentsConfig().getBoolean("OraclesOfEnchantment.OraclesOfEnchantment-Trader-Enabled");

        Material itemMaterial = Material.valueOf(main.getEnchantmentsConfig().getString("OraclesOfEnchantment.OraclesOfEnchantment-Trader-Cost-Item"));

        int itemAmount = main.getEnchantmentsConfig().getInt("OraclesOfEnchantment.OraclesOfEnchantment-Trader-Cost-Amount");

        boolean isTraderSlotRandom = main.getEnchantmentsConfig().getBoolean("OraclesOfEnchantment.OraclesOfEnchantment-Trader-Slot-Random");

        int itemSlot = main.getEnchantmentsConfig().getInt("OraclesOfEnchantment.OraclesOfEnchantment-Trader-Slot");

        if(!isTraderEnabled) return;

        if(e.getEntity() instanceof WanderingTrader)
        {
            WanderingTrader trader = (WanderingTrader) e.getEntity();

            if(Math.random() < chanceOfSpawning)
            {
                // Create an instance of CreateCustomEnchant and call the method
                CreateOracleBook createOracle = new CreateOracleBook(main);
                ItemStack oracleItem = createOracle.CreateOracleItem(1, null);

                // Trade Recipe
                MerchantRecipe recipe = new MerchantRecipe(oracleItem, 1);
                recipe.addIngredient(new ItemStack(itemMaterial, itemAmount));

                if(isTraderSlotRandom)
                {
                    trader.setRecipe(ThreadLocalRandom.current().nextInt(1, 5), recipe);
                }else {
                    trader.setRecipe(itemSlot, recipe);
                }
            }
        }
    }
}
