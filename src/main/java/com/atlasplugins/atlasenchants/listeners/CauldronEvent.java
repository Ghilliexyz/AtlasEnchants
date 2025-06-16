package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateScrapOfCirce;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CauldronEvent implements Listener {

    private Main main;

    public CauldronEvent(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onCauldronUseage(PlayerInteractEvent e) {
        Block block = (Block) e.getClickedBlock();
        if(block == null) return;
        Material material = block.getType();
        Player player = e.getPlayer();

        if (material != Material.WATER_CAULDRON) return;

        ItemStack item = e.getItem();
        if (item == null) return;

        Material itemType = item.getType();

        ItemStack itemInHand = null;

        EquipmentSlot hand = e.getHand(); // MAIN_HAND or OFF_HAND
        if (hand == EquipmentSlot.HAND) {
            itemInHand = player.getInventory().getItemInMainHand();
        } else if (hand == EquipmentSlot.OFF_HAND) {
            itemInHand = player.getInventory().getItemInOffHand();
        }

        if(itemInHand == null) return;

        ItemMeta itemMeta = itemInHand.getItemMeta();
        if(itemMeta == null) return;
        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();

        if (itemType != Material.valueOf(main.getSettingsConfig().getString("EnchantItems.EnchantItem"))) return;

        if(itemPDC.has(Main.customEnchantKeys, PersistentDataType.STRING))
        {
            String enchantmentData = itemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);
            String[] enchantments = enchantmentData.split(":");
            if (enchantments.length < 2) return;
            // Add the enchantment name and level to the Map
            String enchantmentName = enchantments[0];
            int enchantmentLevel = Integer.parseInt(enchantments[1]);

            boolean isScrapOfCirceEnabled = main.getEnchantmentsConfig().getBoolean("ScrapOfCirceWeave.ScrapOfCirceWeave-Enabled");

            if(!isScrapOfCirceEnabled)
            {
                PlayScrappedDisabledMessageAndSound(player, enchantmentName, enchantmentLevel);
                return;
            }

            BlockData data = block.getBlockData();
            if (data instanceof Levelled) {
                Levelled levelled = (Levelled) data;
                if (levelled.getLevel() > 2) {
                    // Empty the cauldron
                    block.setType(Material.CAULDRON);

                    // Remove 1 item from the player's hand reliably
                    if (hand == EquipmentSlot.HAND) {
                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    } else if (hand == EquipmentSlot.OFF_HAND) {
                        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                    }

                    // Create an instance of CreateScrapeOfCirce and call the method
                    CreateScrapOfCirce createScrapOfCirce = new CreateScrapOfCirce(main);
                    createScrapOfCirce.CreateScrapOfCirceItem(1, player);

                    Location location = player.getLocation();
                    World world = location.getWorld();

                    if(world != null)
                    {
                        ExperienceOrb orb = (ExperienceOrb) world.spawn(location, ExperienceOrb.class);
                        orb.setExperience(5);
                    }

                    PlayScrappedMessageAndSound(player, enchantmentName, enchantmentLevel);
                }
            }
        }
    }

    private void PlayScrappedMessageAndSound(Player player, String enchantmentName, int enchantmentLevel)
    {
        // Get the bool to check if the user wants to show the successful enchant message
        boolean hasScrappedEnchantmentMessage = main.getSettingsConfig().getBoolean("ScrapOfCirceWeaveMessages.ScrapOfCirceWeave-ScrappedAEnchantment-Message-Toggle");

        // check if the user wants to show the Already Applied message
        if (hasScrappedEnchantmentMessage) {
            // Send Already Applied Message in chat when applying a enchant.
            for (String ScrappedEnchantmentMessage : main.getSettingsConfig().getStringList("ScrapOfCirceWeaveMessages.ScrapOfCirceWeave-ScrappedAEnchantment-Message")) {
                String withPAPISet2 = main.setPlaceholders(player, ScrappedEnchantmentMessage);
                String message = Main.color(withPAPISet2)
                        .replace("{enchantmentName}", formatEnchantmentName(enchantmentName))
                        .replace("{enchantmentLevel}", String.valueOf(enchantmentLevel));
                player.sendMessage(message);
            }
        }

        // Get the bool to check if the user wants to play the blacklisted enchant sound
        boolean hasScrappedEnchantmentSound = main.getSettingsConfig().getBoolean("ScrapOfCirceWeaveSounds.ScrapOfCirceWeave-ScrappedAEnchantment-Sound-Toggle");

        // check if the user wants to play the Already Applied sound
        if (hasScrappedEnchantmentSound) {
            // Get apply sound via config.
            Sound scrappedEnchantmentSound = Sound.valueOf(main.getSettingsConfig().getString("ScrapOfCirceWeaveSounds.ScrapOfCirceWeave-ScrappedAEnchantment-Sound"));
            float scrappedEnchantmentVolume = main.getSettingsConfig().getInt("ScrapOfCirceWeaveSounds.ScrapOfCirceWeave-ScrappedAEnchantment-Volume");
            float scrappedEnchantmentPitch = main.getSettingsConfig().getInt("ScrapOfCirceWeaveSounds.ScrapOfCirceWeave-ScrappedAEnchantment-Pitch");
            // Play sound for when enchant is Already Applied.
            player.playSound(player.getLocation(), scrappedEnchantmentSound, scrappedEnchantmentVolume, scrappedEnchantmentPitch);
        }
    }

    private void PlayScrappedDisabledMessageAndSound(Player player, String enchantmentName, int enchantmentLevel)
    {
        // Get the bool to check if the user wants to show the Disabled message
        boolean hasScrappedEnchantmentDisabledMessage = main.getSettingsConfig().getBoolean("ScrapOfCirceWeaveMessages.ScrapOfCirceWeave-DisabledScrapEnchantment-Message-Toggle");

        // check if the user wants to show the Disabled message
        if (hasScrappedEnchantmentDisabledMessage) {
            // Send Disabled Message in chat.
            for (String ScrappedEnchantmentDisabledMessage : main.getSettingsConfig().getStringList("ScrapOfCirceWeaveMessages.ScrapOfCirceWeave-DisabledScrapEnchantment-Message")) {
                String withPAPISet2 = main.setPlaceholders(player, ScrappedEnchantmentDisabledMessage);
                String message = Main.color(withPAPISet2)
                        .replace("{enchantmentName}", formatEnchantmentName(enchantmentName))
                        .replace("{enchantmentLevel}", String.valueOf(enchantmentLevel));
                player.sendMessage(message);

            }
        }

        // Get the bool to check if the user wants to play the blacklisted enchant sound
        boolean hasScrappedEnchantmentDisabledSound = main.getSettingsConfig().getBoolean("ScrapOfCirceWeaveSounds.ScrapOfCirceWeave-DisabledScrapEnchantment-Sound-Toggle");

        // check if the user wants to play the Already Applied sound
        if (hasScrappedEnchantmentDisabledSound) {
            // Get apply sound via config.
            Sound scrappedEnchantmentDisabledSound = Sound.valueOf(main.getSettingsConfig().getString("ScrapOfCirceWeaveSounds.ScrapOfCirceWeave-DisabledScrapEnchantment-Sound"));
            float scrappedEnchantmentDisabledVolume = main.getSettingsConfig().getInt("ScrapOfCirceWeaveSounds.ScrapOfCirceWeave-DisabledScrapEnchantment-Volume");
            float scrappedEnchantmentDisabledPitch = main.getSettingsConfig().getInt("ScrapOfCirceWeaveSounds.ScrapOfCirceWeave-DisabledScrapEnchantment-Pitch");
            // Play sound for when enchant is Already Applied.
            player.playSound(player.getLocation(), scrappedEnchantmentDisabledSound, scrappedEnchantmentDisabledVolume, scrappedEnchantmentDisabledPitch);
        }
    }

    private String formatEnchantmentName(String enchantmentName){
        if (enchantmentName == null) {
            return "";
        }
        // Replace periods with spaces
        String formattedName = enchantmentName.replace('-', ' ');

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
