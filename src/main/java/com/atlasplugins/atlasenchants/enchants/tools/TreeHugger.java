package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
    public TreeHugger (Main main) {
        this.main = main;
    }

    private int removeDurability = 0;

    private static final Set<Material> LOGS;
    static {
        Set<Material> logs = new HashSet<>();
        Collections.addAll(logs,
                Material.OAK_LOG,
                Material.SPRUCE_LOG,
                Material.BIRCH_LOG,
                Material.JUNGLE_LOG,
                Material.ACACIA_LOG,
                Material.DARK_OAK_LOG,
                Material.CHERRY_LOG,
                Material.MANGROVE_LOG,
                Material.MANGROVE_ROOTS
        );
        LOGS = Collections.unmodifiableSet(logs);
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
    public void onBreak(BlockBreakEvent e) {
        //Grabbing the player
        Player p = e.getPlayer();

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
                            ItemStack item = p.getInventory().getItemInMainHand();
                            ItemMeta itemMeta = item.getItemMeta();

                            Block block = e.getBlock();
                            if (LOGS.contains(block.getType()) && !main.getLogsPlacedManager().isPlayerPlacedLog(block)) {

                                chopTree(block, item);

                                if(itemMeta instanceof Damageable)
                                {
                                    int currentDurability = item.getDurability();

                                    int newItemDurability = (currentDurability + removeDurability);

                                    // Manually remove durability
                                    item.setDurability((short) newItemDurability);

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
            if (LOGS.contains(nblock.getType()) && !main.getLogsPlacedManager().isPlayerPlacedLog(nblock)) {
                findLogs(nblock, logsToChop);
            }
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        Block blockPlaced = e.getBlockPlaced();
        Material blockMaterial = blockPlaced.getType();

        if(LOGS.contains(blockMaterial)) {
            main.getLogsPlacedManager().markPlayerPlacedLog(blockPlaced);
            // Save data to file
            main.getLogsPlacedManager().saveDataToFile();
        }
    }
}
