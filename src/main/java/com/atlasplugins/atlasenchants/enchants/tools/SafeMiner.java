package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class SafeMiner implements Listener {

    private Main main;
    public SafeMiner (Main main) {
        this.main = main;
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
        Player p = e.getPlayer();

        // Check if the player has an enchanted tool
        if (!hasTool(p)) return;

        // Get Enchantment Enabled Status
        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.SAFE-MINER.Enchantment-Enabled");
        // if Enchantment Enabled = false return.
        if (!isEnchantmentEnabled) return;

        PersistentDataContainer enchantedItemPDC = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
        String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

        // Ensure the enchantment data is not null or empty
        if (enchantedItemData == null || enchantedItemData.isEmpty()) return;
        String[] enchantments = enchantedItemData.split(",");

        for (String enchantment : enchantments) {
            String[] enchantParts = enchantment.split(":");

            // Ensure the format is correct
            if (enchantParts.length != 2) return;

            String enchantName = enchantParts[0];
            int enchantLevel = Integer.parseInt(enchantParts[1]);

            if (enchantName.contains("SAFE-MINER")) {
                // PUT ENCHANT LOGIC HERE
                Block blockMined = e.getBlock();
                // Handle block drops manually
                ItemStack tool = p.getInventory().getItemInMainHand();

                SafeMinerLogic(blockMined, tool, p);

                // Prevent default drops
                e.setDropItems(false);
                //END ENCHANT LOGIC
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
        }else {
            // Check for attached items like Torches
//            handleAttachedItems(blockMined, tool, drops);
        }

        // Add drops to the player's inventory or drop them if the inventory is full
        for (ItemStack drop : drops) {
            HashMap<Integer, ItemStack> leftItems = player.getInventory().addItem(drop);
            if (!leftItems.isEmpty()) {
                for (ItemStack item : leftItems.values()) {
                    blockMined.getWorld().dropItemNaturally(blockMined.getLocation(), item);
                }
            }
        }
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

    private void handleAttachedItems(Block block, ItemStack tool, Collection<ItemStack> drops) {
//        block.breakNaturally(tool);

        // Check for attached items like torches
//        for (BlockFace face : BlockFace.values()) {
//            Block attachedBlock = block.getRelative(face);
//            if (attachedBlock.getType() == Material.TORCH) {
//                // Drop torch item
//                drops.add(new ItemStack(Material.TORCH));
//                attachedBlock.setType(Material.AIR); // Break the torch block
//            }
//            // Add more checks for other attached items as needed
//        }
    }
}
