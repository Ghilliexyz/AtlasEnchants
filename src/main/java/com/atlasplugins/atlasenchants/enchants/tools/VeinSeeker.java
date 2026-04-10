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
import org.bukkit.entity.ExperienceOrb;
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

public class VeinSeeker implements Listener {

    private Main main;
    private WorldGuardPlugin worldGuardPlugin;

    private static final int MAX_BLOCKS = 256;

    public VeinSeeker (Main main) {
        this.main = main;
        this.worldGuardPlugin = main.getWorldGuardPlugin();
    }

    public boolean hasTool (Player p) {
        // Get the player's tool(s)
        ItemStack tool = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.VEIN-SEEKER.Enchantment-Apply-Item");

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
        main.getOresPlacedManager().removePlayerPlacedOre(e.getBlock());
        // Save data to file
        main.getOresPlacedManager().saveDataToFile();

        // Check if the player has an enchanted tool
        if(!hasTool(p)) return;

        // Get Enchantment Enabled Status
        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.VEIN-SEEKER.Enchantment-Enabled");
        // if Enchantment Enabled = false return.
        if(!isEnchantmentEnabled) return;

        // Get the block list.
        List<String> ores = main.getEnchantmentsConfig().getStringList("Enchantments.VEIN-SEEKER.VeinSeeker-Block-List");

        for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
            if (enchant.name.contains("VEIN-SEEKER")) {
                // PUT ENCHANT LOGIC HERE
                ItemStack tool = p.getInventory().getItemInMainHand();
                ItemMeta toolMeta = tool.getItemMeta();

                if (ores.contains(blockBroken.getType().toString()) && !main.getOresPlacedManager().isPlayerPlacedOre(blockBroken)) {

                    double blockXP = e.getExpToDrop();

                    int blocksMined = mineOres(blockBroken, blockXP, tool, p, ores);

                    if (toolMeta instanceof Damageable damageableToolMeta) {
                        // Manually remove durability
                        damageableToolMeta.setDamage(damageableToolMeta.getDamage() + blocksMined);

                        // Apply the modified meta back to the item
                        tool.setItemMeta(damageableToolMeta);
                    }
                }
                //END ENCHANT LOGIC
            }
        }
    }

    // Breaks the blocks and returns how many were mined (excluding start block, handled by the event).
    private int mineOres(Block startBlock, double blockXP, ItemStack tool, Player p, List<String> ores) {
        Set<Block> oresToMine = new HashSet<>();
        findOres(startBlock, oresToMine, ores);

        boolean hasSafeMiner = hasSafeMinerEnchant(tool);

        for (Block ore : oresToMine) {
            // Skip the start block — the BlockBreakEvent (and SafeMiner if present) handles it
            if (ore.equals(startBlock)) continue;

            if (hasSafeMiner) {
                // Collect drops and send to player's inventory instead of dropping on ground
                Collection<ItemStack> drops = ore.getDrops(tool, p);
                for (ItemStack drop : drops) {
                    HashMap<Integer, ItemStack> leftovers = p.getInventory().addItem(drop);
                    for (ItemStack leftover : leftovers.values()) {
                        ore.getWorld().dropItemNaturally(ore.getLocation(), leftover);
                    }
                }
                ore.setType(Material.AIR);
            } else {
                ore.breakNaturally(tool);
            }
        }

        // Spawn a single XP orb for chain blocks (start block XP is handled by the event)
        int chainBlocks = oresToMine.size() - 1;
        int totalXP = (int) (blockXP * chainBlocks);
        if (totalXP > 0) {
            ExperienceOrb experienceOrb = startBlock.getWorld().spawn(startBlock.getLocation(), ExperienceOrb.class);
            experienceOrb.setExperience(totalXP);
        }

        return chainBlocks;
    }

    private boolean hasSafeMinerEnchant(ItemStack tool) {
        if (tool == null || tool.getItemMeta() == null) return false;
        String enchantData = tool.getItemMeta().getPersistentDataContainer().get(Main.customEnchantKeys, PersistentDataType.STRING);
        return enchantData != null && enchantData.contains("SAFE-MINER");
    }

    // Iterative block search with a max block cap to prevent stack overflow.
    private void findOres(Block start, Set<Block> oresToMine, List<String> ores) {
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);

        while (!queue.isEmpty() && oresToMine.size() < MAX_BLOCKS) {
            Block block = queue.poll();
            if (oresToMine.contains(block)) continue;

            oresToMine.add(block);

            List<Block> nearbyBlocks = main.blockRadiusFinder.getBlocks(block, 1, 1, 1);

            for (Block nblock : nearbyBlocks) {
                if (!oresToMine.contains(nblock) && ores.contains(nblock.getType().toString()) && !main.getOresPlacedManager().isPlayerPlacedOre(nblock)) {
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
        List<String> ores = main.getEnchantmentsConfig().getStringList("Enchantments.VEIN-SEEKER.VeinSeeker-Block-List");

        if(ores.contains(blockMaterial.toString())) {
            main.getOresPlacedManager().markPlayerPlacedOre(blockPlaced);
            // Save data to file
            main.getOresPlacedManager().saveDataToFile();
        }
    }
}
