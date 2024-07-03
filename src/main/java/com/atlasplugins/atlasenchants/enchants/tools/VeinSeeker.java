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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinSeeker implements Listener {

    private Main main;
    public VeinSeeker (Main main) {
        this.main = main;
    }

    private int removeDurability = 0;

    private static final Set<Material> ORES;
    static {
        Set<Material> ores = new HashSet<>();
        Collections.addAll(ores,
                Material.COAL_ORE,
                Material.COPPER_ORE,
                Material.IRON_ORE,
                Material.LAPIS_ORE,
                Material.REDSTONE_ORE,
                Material.GOLD_ORE,
                Material.EMERALD_ORE,
                Material.DIAMOND_ORE,
                Material.DEEPSLATE_COAL_ORE,
                Material.DEEPSLATE_COPPER_ORE,
                Material.DEEPSLATE_IRON_ORE,
                Material.DEEPSLATE_LAPIS_ORE,
                Material.DEEPSLATE_REDSTONE_ORE,
                Material.DEEPSLATE_GOLD_ORE,
                Material.DEEPSLATE_EMERALD_ORE,
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.NETHER_QUARTZ_ORE,
                Material.NETHER_GOLD_ORE,
                Material.ANCIENT_DEBRIS
        );
        ORES = Collections.unmodifiableSet(ores);
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
    public void onBreak(BlockBreakEvent e) {
        //Grabbing the player
        Player p = e.getPlayer();

        // Remove block location data
        main.getOresPlacedManager().removePlayerPlacedLog(e.getBlock());
        // Save data to file
        main.getOresPlacedManager().saveDataToFile();

        // Check if the player has an enchanted tool
        if(hasTool(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.VEIN-SEEKER.Enchantment-Enabled");
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

                        if (enchantName.contains("VEIN-SEEKER")) {
                            // PUT ENCHANT LOGIC HERE
                            ItemStack item = p.getInventory().getItemInMainHand();
                            ItemMeta itemMeta = item.getItemMeta();

                            Block block = e.getBlock();
                            if (ORES.contains(block.getType()) && !main.getOresPlacedManager().isPlayerPlacedLog(block)) {

                                mineOres(block, item);

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
    private void mineOres(Block block, ItemStack tool) {
        Set<Block> oresToMine = new HashSet<>();
        findOres(block, oresToMine);
        for (Block ore : oresToMine) {
            ore.breakNaturally(tool);
            removeDurability++;
        }
    }

    // Finds the blocks to break.
    private void findOres(Block block, Set<Block> logsToChop) {
        if (logsToChop.contains(block)) return;
        logsToChop.add(block);

        List<Block> nearbyBlocks = main.blockRadiusFinder.getBlocks(block, 1, 1, 1);

        for (Block nblock : nearbyBlocks) {
            if (ORES.contains(nblock.getType()) && !main.getOresPlacedManager().isPlayerPlacedLog(nblock)) {
                findOres(nblock, logsToChop);
            }
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        Block blockPlaced = e.getBlockPlaced();
        Material blockMaterial = blockPlaced.getType();

        if(ORES.contains(blockMaterial)) {
            main.getOresPlacedManager().markPlayerPlacedLog(blockPlaced);
            // Save data to file
            main.getOresPlacedManager().saveDataToFile();
        }
    }
}
