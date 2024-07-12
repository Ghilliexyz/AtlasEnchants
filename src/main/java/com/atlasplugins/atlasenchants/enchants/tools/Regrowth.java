package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Regrowth implements Listener {

    private Main main;
    public Regrowth (Main main) {
        this.main = main;
    }

    public boolean hasTool (Player p) {
        // Get the player's tool(s)
        ItemStack tool = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.REGROWTH.Enchantment-Apply-Item");

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
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.REGROWTH.Enchantment-Enabled");
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

                        if (enchantName.contains("REGROWTH")) {
                            // PUT ENCHANT LOGIC HERE
                            Block cropBroken = e.getBlock();
                            Material cropBrokenMat = cropBroken.getType();
                            BlockData cropBrokenData = cropBroken.getBlockData();

                            ItemStack tool = p.getInventory().getItemInMainHand();
                            ItemMeta toolMeta = tool.getItemMeta();

                            // Check if the block is ageable (crop)
                            if (!(cropBrokenData instanceof Ageable)) {
                                return;
                            }

                            Location cropLoc = cropBroken.getLocation();

                            // Check if the block below is farmland
                            if (cropLoc.clone().subtract(0, 1, 0).getBlock().getType() != Material.FARMLAND) {
                                return;
                            }

                            if (((Ageable) cropBrokenData).getAge() == ((Ageable) cropBrokenData).getMaximumAge()) {
                                // Collect the drops manually
                                Collection<ItemStack> drops = cropBroken.getDrops();

                                // Drop the items manually at the block's location
                                for (ItemStack drop : drops) {
                                    cropLoc.getWorld().dropItemNaturally(cropLoc, drop);
                                }
                            }

                            // Determine the type of crop and replant it as the initial stage
                            Material seedType;
                            switch (cropBrokenMat) {
                                case WHEAT:
                                    seedType = Material.WHEAT;
                                    break;
                                case CARROTS:
                                    seedType = Material.CARROT;
                                    break;
                                case POTATOES:
                                    seedType = Material.POTATO;
                                    break;
                                case BEETROOTS:
                                    seedType = Material.BEETROOTS;
                                    break;
                                default:
                                    return;
                            }

                            // Plant the crop by setting the block type to the crop type (not the seed)
                            cropBroken.setType(cropBrokenMat);

                            // Set the crop's age to 0 (seedling state)
                            BlockData newCropData = cropLoc.getBlock().getBlockData();
                            if (newCropData instanceof Ageable) {
                                Ageable ageableCrop = (Ageable) newCropData;
                                ageableCrop.setAge(0);  // Set age to 0 (seedling state)
                                cropLoc.getBlock().setBlockData(ageableCrop);
                            }

                            // Force a block update to ensure the change is reflected
                            cropLoc.getBlock().getState().update(true, false);

                            if (toolMeta instanceof Damageable) {
                                Damageable damageableToolMeta = (Damageable) toolMeta;

                                damageableToolMeta.setDamage(damageableToolMeta.getDamage() + 1);

                                tool.setItemMeta(toolMeta);  // Don't forget to set the item meta back to the tool
                            }

                            e.setCancelled(true);
                            // END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }
}
