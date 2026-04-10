package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TreeHugger implements Listener {

    private Main main;
    private WorldGuardPlugin worldGuardPlugin;

    private static final int MAX_BLOCKS = 256;

    public TreeHugger (Main main) {
        this.main = main;
        this.worldGuardPlugin = main.getWorldGuardPlugin();
    }

    public boolean hasTool (Player p) {
        // Get the player's tool(s)
        ItemStack tool = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.TREE-HUGGER.Enchantment-Apply-Item");

        // Check if the player has a tool in their hand
        return tool != null && armorMat.contains(tool.getType().toString());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.isCancelled()) return;
        //Grabbing the player
        Player p = e.getPlayer();
        // Grabbing the broken block
        Block blockBroken = e.getBlock();

        //WorldGuard Checks
        if(worldGuardPlugin != null && worldGuardPlugin.isEnabled() && !p.isOp())
        {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(blockBroken.getWorld()));

            if (regions != null) {
                ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.asBlockVector(blockBroken.getLocation()));

                for (ProtectedRegion region : set) {
                    if (!region.getMembers().contains(worldGuardPlugin.wrapPlayer(p)) &&
                            !region.getOwners().contains(worldGuardPlugin.wrapPlayer(p))) {
                        // Cancel the event if the player is not a member or owner of the region
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Remove block location data
        main.getLogsPlacedManager().removePlayerPlacedLog(e.getBlock());
        // Save data to file
        main.getLogsPlacedManager().saveDataToFile();

        // Check if the player has an enchanted tool
        if(!hasTool(p)) return;

        // Get Enchantment Enabled Status
        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.TREE-HUGGER.Enchantment-Enabled");
        // if Enchantment Enabled = false return.
        if(!isEnchantmentEnabled) return;

        // Get the block list.
        List<String> logs = main.getEnchantmentsConfig().getStringList("Enchantments.TREE-HUGGER.TreeHugger-Block-List");

        for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
            if (enchant.name.contains("TREE-HUGGER")) {
                // PUT ENCHANT LOGIC HERE
                ItemStack tool = p.getInventory().getItemInMainHand();
                ItemMeta toolMeta = tool.getItemMeta();

                if (logs.contains(blockBroken.getType().toString()) && !main.getLogsPlacedManager().isPlayerPlacedLog(blockBroken)) {

                    int blocksChopped = chopTree(blockBroken, tool, logs);

                    if (toolMeta instanceof Damageable damageableToolMeta) {
                        // Manually remove durability
                        damageableToolMeta.setDamage(damageableToolMeta.getDamage() + blocksChopped);

                        // Apply the modified meta back to the item
                        tool.setItemMeta(damageableToolMeta);
                    }
                }
                //END ENCHANT LOGIC
            }
        }
    }

    // Breaks the blocks and returns how many were chopped.
    private int chopTree(Block block, ItemStack tool, List<String> logs) {
        Set<Block> logsToChop = new HashSet<>();
        findLogs(block, logsToChop, logs);
        for (Block log : logsToChop) {
            log.breakNaturally(tool);
        }
        return logsToChop.size();
    }

    // Iterative block search with a max block cap to prevent stack overflow.
    private void findLogs(Block start, Set<Block> logsToChop, List<String> logs) {
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty() && logsToChop.size() < MAX_BLOCKS) {
            Block block = queue.poll();
            if (logsToChop.contains(block)) continue;

            logsToChop.add(block);

            List<Block> nearbyBlocks = main.blockRadiusFinder.getBlocks(block, 1, 1, 1);

            for (Block nblock : nearbyBlocks) {
                if (!logsToChop.contains(nblock) && logs.contains(nblock.getType().toString()) && !main.getLogsPlacedManager().isPlayerPlacedLog(nblock)) {
                    queue.add(nblock);
                }
            }
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        Block blockPlaced = e.getBlockPlaced();
        Material blockMaterial = blockPlaced.getType();

        // Get the block list.
        List<String> logs = main.getEnchantmentsConfig().getStringList("Enchantments.TREE-HUGGER.TreeHugger-Block-List");

        if(logs.contains(blockMaterial.toString())) {
            main.getLogsPlacedManager().markPlayerPlacedLog(blockPlaced);
            // Save data to file
            main.getLogsPlacedManager().saveDataToFile();
        }
    }
}
