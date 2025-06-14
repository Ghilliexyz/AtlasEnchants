package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
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
        Action action = e.getAction();

        if(e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.ENCHANTING_TABLE) return;
        if(!action.toString().contains("RIGHT_CLICK")) return;

        Block clickedBlock = e.getClickedBlock();
        BlockStateMeta metaBlock = (BlockStateMeta) new ItemStack(Material.ENCHANTING_TABLE).getItemMeta();

    }
}
