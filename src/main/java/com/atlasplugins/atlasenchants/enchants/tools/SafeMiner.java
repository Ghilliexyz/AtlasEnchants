package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateAltarOfCirce;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class SafeMiner implements Listener {

    private Main main;
    private WorldGuardPlugin worldGuardPlugin;

    public SafeMiner(Main main) {
        this.main = main;
        this.worldGuardPlugin = main.getWorldGuardPlugin();
    }

    public boolean hasTool (Player p) {
        // Get the player's tool(s)
        ItemStack tool = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.SAFE-MINER.Enchantment-Apply-Item");

        // Check if the player has a tool in their hand
        return tool != null && armorMat.contains(tool.getType().toString());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.isCancelled()) return;
        // Grabbing the player
        Player player = e.getPlayer();
        // Grabbing the broken block
        Block blockMined = e.getBlock();

        //WorldGuard Checks
        if(worldGuardPlugin != null && worldGuardPlugin.isEnabled() && !player.isOp())
        {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(blockMined.getWorld()));

            if (regions != null) {
                ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.asBlockVector(blockMined.getLocation()));

                for (ProtectedRegion region : set) {
                    if (!region.getMembers().contains(worldGuardPlugin.wrapPlayer(player)) &&
                            !region.getOwners().contains(worldGuardPlugin.wrapPlayer(player))) {
                        // Cancel the event if the player is not a member or owner of the region
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Check if the player has an enchanted tool
        if (!hasTool(player)) return;

        // Get Enchantment Enabled Status
        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.SAFE-MINER.Enchantment-Enabled");
        // if Enchantment Enabled = false return.
        if (!isEnchantmentEnabled) return;

        for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(player.getInventory().getItemInMainHand())) {
            if (enchant.name.contains("SAFE-MINER")) {
                    // PUT ENCHANT LOGIC HERE
                    // Handle block drops manually
                    ItemStack tool = player.getInventory().getItemInMainHand();

                    if(blockMined.getType() == Material.ENCHANTING_TABLE)
                    {
                        BlockState blockState = blockMined.getState();
                        if(!(blockState instanceof TileState tileState)) return;
                        PersistentDataContainer blockPDC = tileState.getPersistentDataContainer();
                        if(blockPDC.has(Main.customAltarOfCirceKeys, PersistentDataType.STRING))
                        {
                            String value = blockPDC.get(Main.customAltarOfCirceKeys, PersistentDataType.STRING);
                            if("altar_of_circe".equals(value))
                            {
                                // Create an instance of CreateAltarOfCirce and call the method
                                CreateAltarOfCirce createAltarOfCirce = new CreateAltarOfCirce(main);
                                // Drop the custom altar instead
                                createAltarOfCirce.CreateAltarOfCirceItem(1, player);
                                // Prevent default drops
                                e.setDropItems(false);
                            }
                        }
                    }else{
                        SafeMinerLogic(blockMined, e, tool, player);

                        // Prevent default drops
                        e.setDropItems(false);
                        //END ENCHANT LOGIC
                    }
                }
            }
    }

    public void SafeMinerLogic(Block blockMined, BlockBreakEvent event, ItemStack tool, Player player) {
        List<ItemStack> drops = new ArrayList<>(blockMined.getDrops(tool, player));

        if (blockMined.getBlockData() instanceof Bed) {
            // getDrops() only returns the bed from the head part, so grab from other half too
            handleBed(blockMined, tool, drops);
        } else if (blockMined.getBlockData() instanceof Door) {
            // getDrops() only returns the door from the bottom part
            handleDoor(blockMined, tool, drops);
        } else if (isTallFlower(blockMined)) {
            // getDrops() only returns the flower from the bottom part
            handleTallFlower(blockMined, tool, drops);
        } else if (isPiston(blockMined)) {
            // getDrops() only returns the piston from the base when powered
            handlePiston(blockMined, tool, drops);
        } else if (blockMined.getState() instanceof Container) {
            // Check for loot inside Containers like Chests
            handleContainers(blockMined, tool, drops, player);
        } else if(blockMined.getType() == Material.JUKEBOX
                || blockMined.getType() == Material.LECTERN
                || blockMined.getType() == Material.CHISELED_BOOKSHELF
                || blockMined.getType() == Material.DECORATED_POT) {
            handleEdgeCases(blockMined, tool, drops, player);
        }

        // Add drops to the player's inventory or drop them if the inventory is full
        giveOrDropItems(player, drops, blockMined.getLocation());
    }

    private boolean isTallFlower(Block block) {
        return block.getType().equals(Material.ROSE_BUSH) ||
                block.getType().equals(Material.SUNFLOWER) ||
                block.getType().equals(Material.LILAC) ||
                block.getType().equals(Material.PEONY) ||
                block.getType().equals(Material.PITCHER_PLANT);
    }

    private boolean isPiston(Block block) {
        return block.getType().equals(Material.PISTON_HEAD) ||
                block.getType().equals(Material.PISTON) ||
                block.getType().equals(Material.STICKY_PISTON);
    }

    private boolean isShulkerBox(Block block) {
        return block.getType().name().endsWith("SHULKER_BOX");
    }

    private void handleBed(Block block, ItemStack tool, Collection<ItemStack> drops) {
        Bed bedData = (Bed) block.getBlockData();
        Block otherPart = bedData.getPart() == Bed.Part.HEAD ? block.getRelative(bedData.getFacing().getOppositeFace()) : block.getRelative(bedData.getFacing());

        // Add drops for both parts of the bed
        drops.addAll(otherPart.getDrops(tool));
    }

    private void handleDoor(Block block, ItemStack tool, Collection<ItemStack> drops) {
        Door doorData = (Door) block.getBlockData();
        Block otherPart = doorData.getHalf() == Door.Half.TOP ? block.getRelative(0, -1, 0) : block.getRelative(0, 1, 0);

        // Add drops for both parts of the door
        drops.addAll(otherPart.getDrops(tool));
    }

    private void handleTallFlower(Block block, ItemStack tool, Collection<ItemStack> drops) {
        Block upperPart = block.getRelative(BlockFace.UP);
        Block lowerPart = block.getRelative(BlockFace.DOWN);

        // Check if the upper part is a tall flower and add drops
        if (upperPart.getType().equals(block.getType())) {
            drops.addAll(upperPart.getDrops(tool));
        }
        // Check if the lower part is a tall flower and add drops
        if (lowerPart.getType().equals(block.getType())) {
            drops.addAll(lowerPart.getDrops(tool));
        }
    }

    private void handlePiston(Block block, ItemStack tool, Collection<ItemStack> drops) {
        if (block.getType().equals(Material.PISTON_HEAD)) {
            // Find the base of the piston and add drops
            BlockFace[] faces = {BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
            for (BlockFace face : faces) {
                Block relative = block.getRelative(face);
                if (relative.getType().equals(Material.PISTON) || relative.getType().equals(Material.STICKY_PISTON)) {
                    drops.addAll(relative.getDrops(tool));
                    break;
                }
            }
        } else if (block.getType().equals(Material.PISTON) || block.getType().equals(Material.STICKY_PISTON)) {
            // drops already collected from initial getDrops() call
        }
    }

    private void handleContainers(Block block, ItemStack tool, Collection<ItemStack> drops, Player player) {
        BlockState blockState = block.getState();

        // Skip shulker boxes to avoid dupe exploits
        if (isShulkerBox(block)) return;

        if (blockState instanceof Container container) {
            Inventory containerInventory = container.getInventory();

            if (!containerInventory.isEmpty()) {
                for (ItemStack item : containerInventory.getContents()) {
                    if (item != null) {
                        drops.add(item.clone()); // Clone to prevent modifying original reference
                    }
                }
                containerInventory.clear(); // Important: Clear items to prevent duping
            }
        }
    }

    private void handleEdgeCases(Block block, ItemStack tool, Collection<ItemStack> drops, Player player)
    {
        BlockState state = block.getState();

        // Check if state implements InventoryHolder (which it does for JUKEBOX)
        if(state instanceof InventoryHolder inventoryHolder)
        {
            Inventory inv = inventoryHolder.getInventory();

            for (ItemStack item : inv.getContents())
            {
                if(item != null && item.getType() != Material.AIR)
                {
                    drops.add(item.clone());
                }
            }
        }
    }

    private void giveOrDropItems(Player player, Collection<ItemStack> items, Location dropLocation) {
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) continue;

            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(dropLocation, leftover);
            }
        }
    }

}
