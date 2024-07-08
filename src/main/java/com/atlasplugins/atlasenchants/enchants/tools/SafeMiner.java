package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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

                // Break the block naturally
                // fix for deleting touches on the block.
//                blockMined.breakNaturally();
                //END ENCHANT LOGIC
            }
        }
    }

    public void SafeMinerLogic(Block blockMined, ItemStack tool, Player p) {
        // Get the drops using the player's tool
        Collection<ItemStack> drops = blockMined.getDrops(tool, p);

        if (drops.isEmpty()) {return;}

        // Add drops to the player's inventory
        for (ItemStack drop : drops) {
            HashMap<Integer, ItemStack> leftItems = p.getInventory().addItem(drop);
            // If inventory is full, drop the items at the block location
            if (!leftItems.isEmpty()) {
                for (ItemStack item : leftItems.values()) {
                    blockMined.getWorld().dropItemNaturally(blockMined.getLocation(), item);
                }
            }
        }
    }
}
