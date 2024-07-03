package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
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
    public void onBreak(BlockBreakEvent e) {
        //Grabbing the player
        Player p = e.getPlayer();

        // Check if the player has an enchanted tool
        if(hasTool(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.SAFE-MINER.Enchantment-Enabled");
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

                        if (enchantName.contains("SAFE-MINER")) {
                            // PUT ENCHANT LOGIC HERE
                            //This grabs the item the user mined
                            Collection<ItemStack> drops = e.getBlock().getDrops();
                            //This gets the item dropped
                            for(ItemStack drop : drops) {
                                e.setDropItems(false);
                                //If it is full it will return message.
                                HashMap<Integer, ItemStack> leftItems = p.getInventory().addItem(drop);
                                //This checks if their inventory is full
                                if(leftItems.size() == 1){
                                    e.setDropItems(true);
                                }
                            }

                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }
}
