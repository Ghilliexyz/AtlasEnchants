package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class TreeHugger implements Listener {

    private Main main;
    private WorldGuardPlugin worldGuardPlugin;

    public TreeHugger (Main main) {
        this.main = main;
        this.worldGuardPlugin = main.getWorldGuardPlugin();
    }

    private int removeDurability = 0;

    private List<String> LOGS;

    public boolean hasTool (Player p) {
        // Get the player's tool(s)
        ItemStack tool = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.TREE-HUGGER.Enchantment-Apply-Item");

        // Check if the player has a tool in their hand
        return tool != null && armorMat.contains(tool.getType().toString());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
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
        if(hasTool(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.TREE-HUGGER.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            // Get the block list.
            LOGS = main.getEnchantmentsConfig().getStringList("Enchantments.TREE-HUGGER.TreeHugger-Block-List");

            PersistentDataContainer enchantedItemPDC = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
            String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

            // Ensure the enchantment data is not null or empty
            if (enchantedItemData != null && !enchantedItemData.isEmpty()) {
                String[] enchantments = enchantedItemData.split(",");

                for (String enchantment : enchantments) {
                    String[] enchantParts = enchantment.split(":");

                    // Ensure the format is correct
                    if (enchantParts.length == 2) {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);

                        if (enchantName.contains("TREE-HUGGER")) {
                            // PUT ENCHANT LOGIC HERE
                            ItemStack tool = p.getInventory().getItemInMainHand();
                            ItemMeta toolMeta = tool.getItemMeta();

                            if (LOGS.contains(blockBroken.getType().toString()) && !main.getLogsPlacedManager().isPlayerPlacedLog(blockBroken)) {

                                chopTree(blockBroken, tool);

                                if(toolMeta instanceof Damageable)
                                {
                                    // Get Damageable tool
                                    Damageable damageableToolMeta = (Damageable) toolMeta;

                                    // Manually remove durability
                                    damageableToolMeta.setDamage(damageableToolMeta.getDamage() + removeDurability);

                                    // Reset durability removal
                                    removeDurability = 0;
                                }
                            }
                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }

    // Breaks the blocks.
    private void chopTree(Block block, ItemStack tool) {
        Set<Block> logsToChop = new HashSet<>();
        findLogs(block, logsToChop);
        for (Block log : logsToChop) {
            log.breakNaturally(tool);
            removeDurability++;
        }
    }

    // Finds the blocks to break.
    private void findLogs(Block block, Set<Block> logsToChop) {
        if (logsToChop.contains(block)) return;
        logsToChop.add(block);

        List<Block> nearbyBlocks = main.blockRadiusFinder.getBlocks(block, 1, 1, 1);

        for (Block nblock : nearbyBlocks) {
            if (LOGS.contains(nblock.getType().toString()) && !main.getLogsPlacedManager().isPlayerPlacedLog(nblock)) {
                findLogs(nblock, logsToChop);
            }
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        Block blockPlaced = e.getBlockPlaced();
        Material blockMaterial = blockPlaced.getType();

        // Get the block list.
        LOGS = main.getEnchantmentsConfig().getStringList("Enchantments.TREE-HUGGER.TreeHugger-Block-List");

        if(LOGS.contains(blockMaterial.toString())) {
            main.getLogsPlacedManager().markPlayerPlacedLog(blockPlaced);
            // Save data to file
            main.getLogsPlacedManager().saveDataToFile();
        }
    }
}
