package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.logging.Level;

public class ApplyShard implements Listener {

    private Main main;
    public ApplyShard(Main main) {this.main = main;}

    @EventHandler
    public void onInventoryClick(InventoryClickEvent invEvent) {
        // Check if the clicked inventory is not null and if it's the player's inventory
        if (invEvent.getClickedInventory() != null && invEvent.getClickedInventory().getType() == InventoryType.PLAYER) {
            // Get the player who clicked
            Player player = (Player) invEvent.getWhoClicked();
            // Get the item that was clicked
            ItemStack clickedItem = invEvent.getCurrentItem();
            // Get the item on the cursor
            ItemStack cursorItem = invEvent.getCursor();

            // Store the enchantment's that will be removed
            String removedEnchantmentName = null;
            // Create a list to hold enchantment names
            Map<String, Integer> removeEnchantmentNameList = new HashMap<>();
            // Store the enchantment's level that will be removed
            int removedEnchantmentLevel = 0;

            // Ensure that neither the clicked item nor the cursor item is null
            if (clickedItem == null || cursorItem == null) return;

            // Get the custom item material type from the config
            Material shardItem = Material.valueOf(main.getSettingsConfig().getString("OblivionShard.OblivionShard-Item"));
            Material enchantmentItem = Material.valueOf(main.getSettingsConfig().getString("EnchantItems.EnchantItem"));

            // If the item on the cursor is not the custom item, return
            if (cursorItem.getType() != shardItem) return;

            // if the clicked item == itself return
            if(clickedItem.getType() == shardItem) return;

            // If the clicked item == the enchantment item return
            if(clickedItem.getType() == enchantmentItem) return;

            // Check if the cursor item has metadata
            if (cursorItem.hasItemMeta()) {
                ItemMeta cursorMeta = cursorItem.getItemMeta();
                PersistentDataContainer cursorPDC = cursorMeta.getPersistentDataContainer();
                // Check if the cursor item has custom shard data
                if (cursorPDC.has(Main.customShardKeys, PersistentDataType.STRING)) {
                    // Get the custom shard data from the cursor item
                    String cursorData = cursorPDC.get(Main.customShardKeys, PersistentDataType.STRING);

                    // Get the metadata of the clicked item
                    ItemMeta itemMeta = clickedItem.getItemMeta();
                    if (itemMeta == null) return; // Ensure itemMeta is not null
                    PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();

                    if (cursorData != null) {
                        // Split the Shard data into name and level
                        String[] shardParts = cursorData.split(":");
                        String shardName = shardParts[0];
                        int shardID = Integer.parseInt(shardParts[1]);

                        // Get Shard Enabled Status
                        boolean isOblivionShardEnabled = main.getSettingsConfig().getBoolean("OblivionShard.OblivionShard-Enabled");

                        if(!isOblivionShardEnabled)
                        {
                            // Get Shard Disabled sound via config.
                            Sound shardDisabledSound = Sound.valueOf(main.getSettingsConfig().getString("ShardItemSounds.ShardItem-DisabledShard-Sound"));
                            float shardDisabledVolume = main.getSettingsConfig().getInt("ShardItemSounds.ShardItem-DisabledShard-Volume");
                            float shardDisabledPitch = main.getSettingsConfig().getInt("ShardItemSounds.ShardItem-DisabledShard-Pitch");

                            // Get the bool to check if the user wants to play the Shard Disabled sound
                            boolean shardDisabledPlaySound = main.getSettingsConfig().getBoolean("ShardItemSounds.ShardItem-DisabledShard-Sound-Toggle");

                            // check if the user doesn't want to play the sound then return if not.
                            if(shardDisabledPlaySound) {
                                // Play sound for when enchant is blacklisted.
                                player.playSound(player.getLocation(), shardDisabledSound, shardDisabledVolume, shardDisabledPitch);
                            }

                            // Get the bool to check if the user wants to show the Shard Disabled message
                            boolean shardDisabledSendMessage = main.getSettingsConfig().getBoolean("ShardItemMessages.ShardItem-DisabledShard-Message-Toggle");
                            // check if the user doesn't want to send the Shard Disabled Message, return if not.
                            if (shardDisabledSendMessage) {
                                // Send blacklisted Message in chat
                                for (String BlacklistMessage : main.getSettingsConfig().getStringList("ShardItemMessages.ShardItem-DisabledShard-Message")) {
                                    String withPAPISet1 = main.setPlaceholders(player, BlacklistMessage);
                                    String message = Main.color(withPAPISet1)
                                            .replace("{shardName}", formatshardName(shardName))
                                            .replace("{shardID}", String.valueOf(shardID));
                                    player.sendMessage(message);
                                }
                            }
                            return;
                        }

                        boolean shouldApplyEnchantment = false;
                        // Check if the clicked item already has enchantments
                        if (itemPDC.has(Main.customEnchantKeys, PersistentDataType.STRING)) {
                            String existingEnchantData = itemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);
                            String[] existingEnchantments = existingEnchantData.split(",");

                            // Iterate through existing enchantments
                            for (String existingEnchant : existingEnchantments) {
                                String[] existingEnchantParts = existingEnchant.split(":");
                                if (existingEnchantParts.length < 2) continue; // Ensure correct format

                                // Add the enchantment name and level to the Map
                                String enchantmentName = existingEnchantParts[0];
                                int enchantmentLevel = Integer.parseInt(existingEnchantParts[1]);

                                removeEnchantmentNameList.put(enchantmentName, enchantmentLevel);

                                shouldApplyEnchantment = true;
                            }
                        }else{
                            // Get apply sound via config.
                            Sound shardNoEnchantsAppliedSound = Sound.valueOf(main.getSettingsConfig().getString("ShardItemSounds.ShardItem-NoEnchantsApplied-Sound"));
                            float shardNoEnchantsAppliedVolume = main.getSettingsConfig().getInt("ShardItemSounds.ShardItem-NoEnchantsApplied-Volume");
                            float shardNoEnchantsAppliedPitch = main.getSettingsConfig().getInt("ShardItemSounds.ShardItem-NoEnchantsApplied-Pitch");

                            // Get the bool to check if the user wants to play the blacklisted enchant sound
                            boolean shardNoEnchantsAppliedPlaySound = main.getSettingsConfig().getBoolean("ShardItemSounds.ShardItem-NoEnchantsApplied-Sound-Toggle");

                            // Get the bool to check if the user wants to show the successful enchant message
                            boolean shardNoEnchantsAppliedSendMessage = main.getSettingsConfig().getBoolean("ShardItemMessages.ShardItem-NoEnchantsApplied-Message-Toggle");

                            // check if the user wants to play the Already Applied sound
                            if(shardNoEnchantsAppliedPlaySound){
                                // Play sound for when enchant is Already Applied.
                                player.playSound(player.getLocation(), shardNoEnchantsAppliedSound, shardNoEnchantsAppliedVolume, shardNoEnchantsAppliedPitch);
                            }
                            // check if the user wants to show the Already Applied message
                            if (shardNoEnchantsAppliedSendMessage)
                            {
                                // Send Already Applied Message in chat when applying a enchant.
                                for (String AlreadyApplyMessage : main.getSettingsConfig().getStringList("ShardItemMessages.ShardItem-NoEnchantsApplied-Message")) {
                                    String withPAPISet2 = main.setPlaceholders(player, AlreadyApplyMessage);
                                    String message = Main.color(withPAPISet2)
                                            .replace("{shardName}", formatshardName(shardName))
                                            .replace("{shardID}", String.valueOf(shardID))
                                            .replace("{removedEnchantmentName}", formatshardName(removedEnchantmentName))
                                            .replace("{removedEnchantmentLevel}", String.valueOf(removedEnchantmentLevel));
                                    player.sendMessage(message);
                                }
                            }

                            return;
                        }

                        // Apply the Shard if it's not already present at an equal or higher level
                        if (shouldApplyEnchantment) {

                            // Check if the Map is not empty before picking a random enchantment
                            if (!removeEnchantmentNameList.isEmpty()) {
                                // Pick a random enchantment from the Map
                                Random random = new Random();
                                Object[] keys = removeEnchantmentNameList.keySet().toArray();
                                String randomKey = (String) keys[random.nextInt(keys.length)];
                                removedEnchantmentName = randomKey;
                                removedEnchantmentLevel = removeEnchantmentNameList.get(randomKey);

                                // Remove an enchant from an item by calling RemoveCustomEnchant and call the method
                                RemoveCustomEnchant removeCustomEnchant = new RemoveCustomEnchant(main);
                                removeCustomEnchant.RemoveEnchantment(clickedItem, removedEnchantmentName);

                                boolean returnEnchantEnabled = main.getSettingsConfig().getBoolean("OblivionShard.OblivionShard-ReturnEnchant-Enabled");

                                if(returnEnchantEnabled){
                                    double returnEnchantChance = main.getSettingsConfig().getDouble("OblivionShard.OblivionShard-ReturnEnchant-Chance");

                                    // of the return enchant chance passes then create the removed enchant.
                                    if(random.nextDouble() > returnEnchantChance) {
                                        // Create an instance of CreateCustomEnchant and call the method
                                        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
                                        ItemStack returnedEnchant = createCustomEnchant.CreateCustomEnchantmentItem(removedEnchantmentName, removedEnchantmentLevel, 1, player);

                                        // Add items to player's inventory if player is not null
                                        for (int i = 0; i < 1; i++) {
                                            // Check if there's space in the player's inventory
                                            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(returnedEnchant);

                                            // If the inventory is full and the item could not be added, drop it at the player's feet
                                            if (!remainingItems.isEmpty()) {
                                                for (ItemStack item : remainingItems.values()) {
                                                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                                                }
                                            }
                                        }

                                        // Get the bool to check if the user wants to show the Success And Refund enchant message
                                        boolean shardApplySendMessage = main.getSettingsConfig().getBoolean("ShardItemMessages.ShardItem-SuccessAndRefund-Message-Toggle");

                                        // check if the user wants to show the Success And Refund message
                                        if (shardApplySendMessage) {
                                            // Send Success And Refund Message in chat when applying a enchant.
                                            for (String SuccessAndRefundMessage : main.getSettingsConfig().getStringList("ShardItemMessages.ShardItem-SuccessAndRefund-Message")) {
                                                String withPAPISet2 = main.setPlaceholders(player, SuccessAndRefundMessage);
                                                String message = Main.color(withPAPISet2)
                                                        .replace("{shardName}", formatshardName(shardName))
                                                        .replace("{shardID}", String.valueOf(shardID))
                                                        .replace("{removedEnchantmentName}", formatshardName(removedEnchantmentName))
                                                        .replace("{removedEnchantmentLevel}", String.valueOf(removedEnchantmentLevel));
                                                player.sendMessage(message);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        // Get the bool to check if the user wants to show the successful enchant message
                                        boolean shardApplySendMessage = main.getSettingsConfig().getBoolean("ShardItemMessages.ShardItem-Success-Message-Toggle");

                                        // check if the user wants to show the Already Applied message
                                        if (shardApplySendMessage)
                                        {
                                            // Send Already Applied Message in chat when applying a enchant.
                                            for (String AlreadyApplyMessage : main.getSettingsConfig().getStringList("ShardItemMessages.ShardItem-Success-Message")) {
                                                String withPAPISet2 = main.setPlaceholders(player, AlreadyApplyMessage);
                                                String message = Main.color(withPAPISet2)
                                                        .replace("{shardName}", formatshardName(shardName))
                                                        .replace("{shardID}", String.valueOf(shardID))
                                                        .replace("{removedEnchantmentName}", formatshardName(removedEnchantmentName))
                                                        .replace("{removedEnchantmentLevel}", String.valueOf(removedEnchantmentLevel));
                                                player.sendMessage(message);
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    // Get the bool to check if the user wants to show the successful enchant message
                                    boolean shardApplySendMessage = main.getSettingsConfig().getBoolean("ShardItemMessages.ShardItem-Success-Message-Toggle");

                                    // check if the user wants to show the Already Applied message
                                    if (shardApplySendMessage)
                                    {
                                        // Send Already Applied Message in chat when applying a enchant.
                                        for (String AlreadyApplyMessage : main.getSettingsConfig().getStringList("ShardItemMessages.ShardItem-Success-Message")) {
                                            String withPAPISet2 = main.setPlaceholders(player, AlreadyApplyMessage);
                                            String message = Main.color(withPAPISet2)
                                                    .replace("{shardName}", formatshardName(shardName))
                                                    .replace("{shardID}", String.valueOf(shardID))
                                                    .replace("{removedEnchantmentName}", formatshardName(removedEnchantmentName))
                                                    .replace("{removedEnchantmentLevel}", String.valueOf(removedEnchantmentLevel));
                                            player.sendMessage(message);
                                        }
                                    }
                                }
                            }

                            // Remove the shard that is left.
                            invEvent.setCursor(new ItemStack(Material.AIR));

                            // Update the inventory to reflect changes
                            player.updateInventory();

                            // Cancel the event to prevent default behavior
                            invEvent.setCancelled(true);

                            // Get apply sound via config.
                            Sound shardApplySound = Sound.valueOf(main.getSettingsConfig().getString("ShardItemSounds.ShardItem-Apply-Sound"));
                            float shardApplyVolume = main.getSettingsConfig().getInt("ShardItemSounds.ShardItem-Apply-Volume");
                            float shardApplyPitch = main.getSettingsConfig().getInt("ShardItemSounds.ShardItem-Apply-Pitch");

                            // Get the bool to check if the user wants to play the blacklisted enchant sound
                            boolean shardApplyPlaySound = main.getSettingsConfig().getBoolean("ShardItemSounds.ShardItem-Apply-Sound-Toggle");

                            // check if the user wants to play the Already Applied sound
                            if(shardApplyPlaySound){
                                // Play sound for when enchant is Already Applied.
                                player.playSound(player.getLocation(), shardApplySound, shardApplyVolume, shardApplyPitch);
                            }
                        }
                    }
                }
            }
        }
    }

    private String formatshardName(String shardName) {
        if(shardName == null){
            return "";
        }
        // Replace periods with spaces
        String formattedName = shardName.replace('-', ' ');

        // Split the name into words
        String[] words = formattedName.split(" ");

        // Capitalize the first letter of each word
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        // Remove trailing space and return the formatted name
        return result.toString().trim();
    }
}
