package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class OraclesTableEvent implements Listener {

    private Main main;

    public OraclesTableEvent(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e)
    {
        if(e.getRecipe() == null) return;

        boolean isOracleTableCraftingEnabled = main.getEnchantmentsConfig().getBoolean("OraclesTable.OraclesTable-Crafting-Enabled");

        if(!isOracleTableCraftingEnabled) return;

        ItemStack result = e.getRecipe().getResult();
        if(result == null || !result.hasItemMeta()) return;

        ItemMeta meta = result.getItemMeta();
        if(!meta.getPersistentDataContainer().has(Main.customOracleTableKeys, PersistentDataType.STRING)) return;

        boolean foundValidBook = false;

        for (ItemStack item : e.getInventory().getMatrix()) {
            if(item == null || !item.hasItemMeta()) continue;

            ItemMeta im = item.getItemMeta();
            if(im.getPersistentDataContainer().has(Main.customOracleBookKeys, PersistentDataType.STRING)) {
                foundValidBook = true;
                break;
            }
        }

        if(!foundValidBook){
            e.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        if (e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.ENCHANTING_TABLE) return;
        if (!e.getAction().toString().contains("RIGHT_CLICK")) return;

        Block block = e.getClickedBlock();
        BlockState state = block.getState();

        if (!(state instanceof TileState tileState)) return;

        PersistentDataContainer pdc = tileState.getPersistentDataContainer();
        String tag = pdc.get(Main.customOracleTableKeys, PersistentDataType.STRING);
        if (tag == null || !tag.equals("oracle_table")) return;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(Main.customOracleTableKeys, PersistentDataType.STRING)) return;

        // It's an oracle table being placed!
        Block block = e.getBlockPlaced();
        BlockState state = block.getState();

        if (state instanceof TileState tileState) {
            tileState.getPersistentDataContainer().set(Main.customOracleTableKeys, PersistentDataType.STRING, "oracle_table");
            tileState.update();
        }
    }

}
