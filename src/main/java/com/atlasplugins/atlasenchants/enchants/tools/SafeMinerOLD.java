package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateAltarOfCirce;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
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

public class SafeMinerOLD implements Listener {

    private static final Logger log = LogManager.getLogger(SafeMinerOLD.class);
    private Main main;
    private WorldGuardPlugin worldGuardPlugin;

    public SafeMinerOLD(Main main) {
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

        PersistentDataContainer enchantedItemPDC = player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
        String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

        // Ensure the enchantment data is not null or empty
        if (enchantedItemData == null || enchantedItemData.isEmpty()) return;
        String[] enchantments = enchantedItemData.split(",");

        for (String enchantment : enchantments) {
            String[] enchantParts = enchantment.split(":");

            // Ensure the format is correct
            if (enchantParts.length == 3) {
                String enchantName = enchantParts[0];
                int enchantLevel = Integer.parseInt(enchantParts[1]);
                int enchantID = Integer.parseInt(enchantParts[2]);

                if (enchantName.contains("SAFE-MINER")) {
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
                        SafeMinerLogic(blockMined, tool, player);

                        // Prevent default drops
                        e.setDropItems(false);
                        //END ENCHANT LOGIC
                    }
                }
            }
        }
    }

    public void SafeMinerLogic(Block blockMined, ItemStack tool, Player player) {
        List<ItemStack> drops = new ArrayList<>(blockMined.getDrops(tool, player));

        if (blockMined.getBlockData() instanceof Bed) {
            // Handle breaking Beds to make sure you can mine any part of it.
            handleBed(blockMined, tool, drops);
        } else if (blockMined.getBlockData() instanceof Door) {
            // Handle breaking Doors to make sure you can mine any part of it.
            handleDoor(blockMined, tool, drops);
        } else if (isTallFlower(blockMined)) {
            // Handle breaking tall flowers
            handleTallFlower(blockMined, tool, drops);
        } else if (isPiston(blockMined)) {
            // Handle breaking Pistons
            handlePiston(blockMined, tool, drops);
        } else if (blockMined.getState() instanceof Container) {
            // Check for loot inside Containers like Chests, half working still need to open it to generate the loot.
            handleContainers(blockMined, tool, drops, player);
        }else if(blockMined.getType() == Material.JUKEBOX
                || blockMined.getType() == Material.LECTERN
                || blockMined.getType() == Material.CHISELED_BOOKSHELF
                || blockMined.getType() == Material.DECORATED_POT) {
            handleEdgeCases(blockMined, tool, drops, player);
        }else {
            // Check for attached items like Torches
            handleAttachedItems(blockMined, player, tool, drops);
        }

        // Add drops to the player's inventory or drop them if the inventory is full
        giveOrDropItems(player, drops, blockMined.getLocation());
//        for (ItemStack drop : drops) {
//            HashMap<Integer, ItemStack> leftItems = player.getInventory().addItem(drop);
//            if (!leftItems.isEmpty()) {
//                for (ItemStack item : leftItems.values()) {
//                    blockMined.getWorld().dropItemNaturally(blockMined.getLocation(), item);
//                }
//            }
//        }
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
        return block.getType().equals(Material.SHULKER_BOX) ||
                block.getType().equals(Material.WHITE_SHULKER_BOX) ||
                block.getType().equals(Material.LIGHT_GRAY_SHULKER_BOX) ||
                block.getType().equals(Material.GRAY_SHULKER_BOX) ||
                block.getType().equals(Material.BLACK_SHULKER_BOX) ||
                block.getType().equals(Material.BROWN_SHULKER_BOX) ||
                block.getType().equals(Material.RED_SHULKER_BOX) ||
                block.getType().equals(Material.ORANGE_SHULKER_BOX) ||
                block.getType().equals(Material.YELLOW_SHULKER_BOX) ||
                block.getType().equals(Material.GREEN_SHULKER_BOX) ||
                block.getType().equals(Material.LIME_SHULKER_BOX) ||
                block.getType().equals(Material.LIGHT_BLUE_SHULKER_BOX) ||
                block.getType().equals(Material.BLUE_SHULKER_BOX) ||
                block.getType().equals(Material.CYAN_SHULKER_BOX) ||
                block.getType().equals(Material.PURPLE_SHULKER_BOX) ||
                block.getType().equals(Material.MAGENTA_SHULKER_BOX) ||
                block.getType().equals(Material.PINK_SHULKER_BOX);
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
                    drops.add((ItemStack) relative);
                    break;
                }
            }
        } else if (block.getType().equals(Material.PISTON) || block.getType().equals(Material.STICKY_PISTON)) {
            // Add drops for the piston base
            drops.add((ItemStack) block.getDrops(tool));
        }
    }


    private void handleContainers(Block block, ItemStack tool, Collection<ItemStack> drops, Player player) {
        BlockState blockState = block.getState();

        // If the block is a container (e.g., chest), add its contents to the drops
        Inventory containerInventory = ((Container) blockState).getInventory();
        // return if the container is a shulker box (prevents duping)
        if(isShulkerBox(block)) return;
        if(!containerInventory.isEmpty()){
            for (ItemStack item : containerInventory.getContents()) {
                if (item != null) {
                    drops.add(item);
                }
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

    private void handleAttachedItems(Block block, Player player, ItemStack tool, Collection<ItemStack> drops) {
        dropAttachedItems(block, player);
        Bukkit.getLogger().info("3");
    }

    private void dropAttachedItems(Block origin, Player player) {
        for (BlockFace face : BlockFace.values()) {
            Block relative = origin.getRelative(face);
            BlockData data = relative.getBlockData();

            if (data instanceof Directional directional) {
                BlockFace attachedFace = directional.getFacing().getOppositeFace();
                if (relative.getRelative(attachedFace).equals(origin)) {
                    dropAndClear(relative, player);
                }
            } else if (data instanceof MultipleFacing multipleFacing) {
                for (BlockFace f : multipleFacing.getAllowedFaces()) {
                    if (multipleFacing.hasFace(f)) {
                        if (relative.getRelative(f).equals(origin)) {
                            dropAndClear(relative, player);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void dropAndClear(Block block, Player player) {
        Material itemDrop = getItemFromBlock(block.getType());
        if (itemDrop != null) {
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(itemDrop));
        }
        block.setType(Material.AIR);
    }

    public Material getItemFromBlock(Material blockType) {
        // --- Buttons & Pressure Plates (They drop themselves) ---
        if (blockType.name().endsWith("_BUTTON") || blockType.name().endsWith("_PRESSURE_PLATE")) {
            return blockType;
        }

        // --- Torches ---
        // Redstone Torch (handles both standing and wall)
        if (blockType.name().contains("REDSTONE_") && blockType.name().contains("TORCH")) {
            return Material.REDSTONE_TORCH;
        }
        // Soul Torch (handles both standing and wall)
        if (blockType.name().contains("SOUL_") && blockType.name().contains("TORCH")) {
            return Material.SOUL_TORCH;
        }
        // Regular Torch (handles both standing and wall)
        if (blockType == Material.TORCH || blockType == Material.WALL_TORCH) {
            return Material.TORCH;
        }

        if(blockType == Material.LANTERN)
        {
            return Material.LANTERN;
        }

        // --- Signs & Hanging Signs ---
        if (blockType.name().endsWith("_WALL_SIGN")) {
            // Converts e.g., OAK_WALL_SIGN to OAK_SIGN
            return Material.valueOf(blockType.name().replace("_WALL_", "_"));
        }
        if (blockType.name().endsWith("_HANGING_SIGN")) {
            return blockType; // Hanging signs drop themselves
        }
        if (blockType.name().endsWith("_WALL_HANGING_SIGN")) {
            // Converts e.g., OAK_WALL_HANGING_SIGN to OAK_HANGING_SIGN
            return Material.valueOf(blockType.name().replace("_WALL_", "_"));
        }


        // --- Levers & Bells ---
        if (blockType == Material.LEVER) return Material.LEVER;
        if (blockType == Material.BELL) return Material.BELL;

        // --- Ladders & Vines ---
        if (blockType == Material.LADDER) return Material.LADDER;
        if (blockType == Material.VINE) return Material.VINE;

        // --- Redstone Components (non-torch) ---
        if (blockType == Material.REDSTONE_WIRE) return Material.REDSTONE;
        if (blockType == Material.REPEATER) return Material.REPEATER;
        if (blockType == Material.COMPARATOR) return Material.COMPARATOR;
        if (blockType == Material.TRIPWIRE_HOOK) return Material.TRIPWIRE_HOOK;
        if (blockType == Material.TRIPWIRE) return Material.STRING; // Tripwire drops string

        // --- Crops ---
        // These typically drop themselves or specific items
        if (blockType.name().endsWith("_STEM") || blockType.name().endsWith("_CROP")) {
            // Most crops drop their seeds + potentially the crop item
            // For simplicity, return the block type for harvestable crops.
            // For unharvested crops, they typically don't drop anything or just seeds.
            // This is where block.getDrops() is more accurate, as it handles age.
            return blockType.isItem() ? blockType : null; // Fallback for specific crops
        }
        // Specific crop block types that are not _CROP or _STEM
        if (blockType == Material.WHEAT || blockType == Material.POTATOES || blockType == Material.CARROTS ||
                blockType == Material.BEETROOTS || blockType == Material.SWEET_BERRY_BUSH ||
                blockType == Material.COCOA) {
            return blockType.isItem() ? blockType : null;
        }

        // --- Flowers & Mushrooms ---
        if (blockType.name().endsWith("_FLOWER") || blockType.name().contains("ROSE_BUSH") ||
                blockType.name().contains("SUNFLOWER") || blockType.name().contains("LILAC") ||
                blockType.name().contains("PEONY") || blockType.name().contains("PITCHER_PLANT")) {
            return blockType;
        }
        if (blockType == Material.BROWN_MUSHROOM || blockType == Material.RED_MUSHROOM ||
                blockType == Material.CRIMSON_FUNGUS || blockType == Material.WARPED_FUNGUS) { // Fungi are similar
            return blockType;
        }
        if (blockType == Material.FLOWER_POT) return Material.FLOWER_POT;

        // --- Saplings ---
        if (blockType.name().endsWith("_SAPLING")) {
            return blockType;
        }

        // --- Doors & Beds (complex, usually handled by getDrops for correct item) ---
        // These usually drop themselves. If you break the bottom, it drops one door item.
        if (blockType.name().endsWith("_DOOR")) {
            return blockType;
        }
        if (blockType.name().endsWith("_BED")) {
            return blockType; // Beds drop themselves
        }

        // --- Rails ---
        if (blockType.name().endsWith("_RAIL") || blockType == Material.RAIL) {
            return blockType;
        }

        // --- Item Frames ---
        if (blockType == Material.ITEM_FRAME || blockType == Material.GLOW_ITEM_FRAME) {
            return blockType;
        }

        // --- Banners, Heads, Skulls ---
        if (blockType.name().endsWith("_BANNER") || blockType.name().endsWith("_HEAD") || blockType.name().endsWith("_SKULL")) {
            return blockType;
        }

        // --- Other common attached/dependent blocks ---
        if (blockType == Material.CONDUIT) return Material.CONDUIT;
        if (blockType == Material.END_ROD) return Material.END_ROD;
        if (blockType == Material.CAVE_VINES || blockType == Material.CAVE_VINES_PLANT) return Material.CAVE_VINES;
        if (blockType == Material.GLOW_LICHEN) return Material.GLOW_LICHEN;
        if (blockType == Material.SHULKER_BOX || blockType.name().endsWith("_SHULKER_BOX")) {
            return Material.SHULKER_BOX; // Shulker boxes drop as a generic shulker box
        }


        // Default fallback: If it's an item, return itself. Otherwise, null.
        // This catches many blocks that are placed and break themselves (e.g., dirt, stone)
        // or simple blocks that don't have complex attachment logic and drop themselves.
        return blockType.isItem() ? blockType : null;
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
