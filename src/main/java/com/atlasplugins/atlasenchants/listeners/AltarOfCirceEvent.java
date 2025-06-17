package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.ApplyCustomEnchant;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateAltarOfCirce;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateRandomCustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AltarOfCirceEvent implements Listener {

    private Main main;

    public AltarOfCirceEvent(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e)
    {
        if(e.getRecipe() == null) return;

        boolean isOracleTableCraftingEnabled = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Crafting-Enabled");

        if(!isOracleTableCraftingEnabled) return;

        ItemStack result = e.getRecipe().getResult();
        if(result == null || !result.hasItemMeta()) return;

        ItemMeta meta = result.getItemMeta();
        if(!meta.getPersistentDataContainer().has(Main.customAltarOfCirceKeys, PersistentDataType.STRING)) return;

        boolean foundValidBook = false;

        for (ItemStack item : e.getInventory().getMatrix()) {
            if(item == null || !item.hasItemMeta()) continue;

            ItemMeta im = item.getItemMeta();
            if(im.getPersistentDataContainer().has(Main.customOracleBookKeys, PersistentDataType.STRING)) {
                foundValidBook = true;
                break;
            }
        }

        if(!foundValidBook){
            e.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent e)
    {
        boolean isAltarOfCirceCraftingEnabled = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Enabled");

        if(!isAltarOfCirceCraftingEnabled) return;

        ItemStack item = e.getItem();
        Player player = e.getEnchanter();

        Block block = e.getEnchantBlock();
        BlockState state = block.getState();

        if (!(state instanceof TileState tileState)) return;

        PersistentDataContainer pdc = tileState.getPersistentDataContainer();
        String tag = pdc.get(Main.customAltarOfCirceKeys, PersistentDataType.STRING);
        if (tag == null || !tag.equals("altar_of_circe")) return;

        int enchantmentTableBtn = e.whichButton();

        switch (enchantmentTableBtn) {
            case 0:
                    e.setCancelled(true);
                break;
            case 1:
                    e.setCancelled(true);
                break;
            case 2:
                    applyRandomCustomEnchantments(main, player, item, 1, e);
                break;
        }
    }

    /**
     * Attempts to apply a specified number of random custom enchantments to an ItemStack.
     * Each enchantment will have a random level up to its defined max level.
     * This method will modify the provided ItemStack directly.
     *
     * @param main The main plugin instance.
     * @param player The player who is receiving the enchantment (used for messages/sounds).
     * @param targetItem The ItemStack to which the random enchantments will be applied.
     * @param numberOfEnchantmentsToApply The desired number of distinct random enchantments to attempt to apply.
     */
    public static void applyRandomCustomEnchantments(Main main, Player player, ItemStack targetItem, int numberOfEnchantmentsToApply, EnchantItemEvent e) {

        boolean canEnchantToolsAndArmour = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-ArmourTools-Enchanter-Enabled");

        if(canEnchantToolsAndArmour){
            if(targetItem.getType() != Material.BOOK)
            {
                ApplyToArmourAndTools(main, player, targetItem, numberOfEnchantmentsToApply);
                e.setCancelled(true);
            }
        }

        boolean canEnchantBook = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Book-Enchanter-Enabled");

        if(canEnchantBook){
            if(targetItem.getType() == Material.BOOK)
            {
                ApplyToBook(main, player, targetItem);
            }
        }
    }

    public static  void ApplyToBook(Main main, Player player, ItemStack targetItem)
    {
        // Basic validation: ensure we have a valid item and a positive number of enchantments to apply.
        if (targetItem == null || targetItem.getType() == Material.AIR) {
//            main.getLogger().warning("Attempted to apply random enchantments with invalid item or count. Item: " + targetItem + ", Count: " + numberOfEnchantmentsToApply);
            return;
        }

        targetItem.setType(Material.AIR);

        // Create an instance of CreateRandomCustomEnchant and call the method
        CreateRandomCustomEnchant createRandomCustomEnchant = new CreateRandomCustomEnchant(main);
        createRandomCustomEnchant.CreateRandomOracleEnchantmentItem(player, 1, true, null);
    }

    public static void ApplyToArmourAndTools(Main main, Player player, ItemStack targetItem, int numberOfEnchantmentsToApply)
    {
        // Basic validation: ensure we have a valid item and a positive number of enchantments to apply.
        if (targetItem == null || targetItem.getType() == Material.AIR || numberOfEnchantmentsToApply <= 0 || targetItem.getType() == Material.BOOK) {
//            main.getLogger().warning("Attempted to apply random enchantments with invalid item or count. Item: " + targetItem + ", Count: " + numberOfEnchantmentsToApply);
            return;
        }

        ItemMeta targetItemMeta = targetItem.getItemMeta();
        if (targetItemMeta != null) {
            PersistentDataContainer itemPDC = targetItemMeta.getPersistentDataContainer();
            // If the item already has our custom enchantment key, do nothing and return.
            if (itemPDC.has(Main.customEnchantKeys, PersistentDataType.STRING)) {
                // You might want to send a message to the player here
                return;
            }
        }

        // Get the 'Enchantments' section from your config.
        ConfigurationSection enchantmentsSection = main.getEnchantmentsConfig().getConfigurationSection("Enchantments");

        // Check if the 'Enchantments' section exists and contains any enchantments.
        if (enchantmentsSection == null || enchantmentsSection.getKeys(false).isEmpty()) {
//            main.getLogger().warning("No enchantments defined in the config under 'Enchantments' section. Cannot apply random enchantments.");
            return;
        }

        // Create a mutable list of all available enchantment names.
        List<String> allAvailableEnchantNames = new ArrayList<>(enchantmentsSection.getKeys(false));
        Random random = new Random();
        int successfullyAppliedCount = 0; // To track how many distinct enchantments were actually added/upgraded.

        // Loop to attempt to apply the desired number of enchantments.
        // The loop continues as long as we haven't hit the target count and there are still enchants to try.
        for (int i = 0; i < numberOfEnchantmentsToApply && !allAvailableEnchantNames.isEmpty(); i++) {
            // Pick a random enchantment name from the list of currently available (unattempted/valid) enchants.
            int randomIndex = random.nextInt(allAvailableEnchantNames.size());
            String chosenEnchantName = allAvailableEnchantNames.get(randomIndex);

            // Get the configuration section for the chosen enchantment.
            ConfigurationSection chosenEnchantConfig = enchantmentsSection.getConfigurationSection(chosenEnchantName);

            // Handle cases where the config for this specific enchantment might be missing or malformed.
            if (chosenEnchantConfig == null) {
//                main.getLogger().warning("Configuration section for random enchantment '" + chosenEnchantName + "' not found. Skipping.");
                allAvailableEnchantNames.remove(randomIndex); // Remove from the pool for this batch
                i--; // Decrement counter to try for another enchantment
                continue;
            }

            // Check if the chosen enchantment is enabled.
            boolean isEnchantmentEnabled = chosenEnchantConfig.getBoolean("Enchantment-Enabled", false);
            if (!isEnchantmentEnabled) {
//                main.getLogger().info("Skipping disabled enchantment: " + chosenEnchantName);
                allAvailableEnchantNames.remove(randomIndex); // Remove from the pool for this batch
                i--; // Decrement counter to try for another enchantment
                continue;
            }

            // Get the max level for this enchantment and choose a random level within that range.
            int maxLevel = chosenEnchantConfig.getInt("Enchantment-MaxLvl", 1);
            int randomLevel = random.nextInt(maxLevel) + 1; // Generates 1 to maxLevel (inclusive)

            // Attempt to apply the enchantment using your existing `applyCustomEnchantment` method.
            // This method handles all the complex logic (blacklist, existing enchants, lore, PDC).
            // It modifies `targetItem` directly and returns `null` if it couldn't apply.
            ItemStack resultOfApplication = ApplyCustomEnchant.applyCustomEnchantment(main, player, targetItem, chosenEnchantName, randomLevel);

            if (resultOfApplication != null) {
                // The enchantment was successfully applied or upgraded.
                successfullyAppliedCount++; // Increment the count of successfully applied *distinct* enchants.
                // Remove the enchantment from the pool for this batch to ensure distinct enchantments are attempted.
                allAvailableEnchantNames.remove(randomIndex);
            } else {
                // The enchantment could not be applied (e.g., due to a blacklist conflict, already max level, or wrong item type).
//                main.getLogger().info("Failed to apply '" + chosenEnchantName + "' (Lvl " + randomLevel + ") to item. Trying another unique enchantment.");
                allAvailableEnchantNames.remove(randomIndex); // Remove it from the pool to avoid re-trying the same failing enchant in this batch.
                i--; // Decrement counter to ensure we still attempt to apply 'numberOfEnchantmentsToApply' distinct enchants.
            }
        }

        // Provide general feedback to the player after all attempts.
        if (successfullyAppliedCount == 0 && numberOfEnchantmentsToApply > 0) {
//            player.sendMessage(Main.color("&cCould not apply any random enchantments to your item."));
        } else if (successfullyAppliedCount > 0) {
//            player.sendMessage(Main.color("&aSuccessfully applied &e" + successfullyAppliedCount + "&a random enchantment(s)!"));
        }

        // Crucial: Update the player's inventory to reflect the changes on the item.
        player.updateInventory();
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(Main.customAltarOfCirceKeys, PersistentDataType.STRING)) return;

        // It's an oracle table being placed!
        Block block = e.getBlockPlaced();
        BlockState state = block.getState();

        if (state instanceof TileState tileState) {
            tileState.getPersistentDataContainer().set(Main.customAltarOfCirceKeys, PersistentDataType.STRING, "altar_of_circe");
            tileState.update();
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        String enchantName = "";

        if(tool != null && tool.hasItemMeta())
        {
            ItemMeta toolMeta = tool.getItemMeta();
            PersistentDataContainer toolPDC = toolMeta.getPersistentDataContainer();
            String enchantedItemData = toolPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

            // Ensure the enchantment data is not null or empty
            if (enchantedItemData == null || enchantedItemData.isEmpty()){

            }else{
                String[] enchantments = enchantedItemData.split(",");

                for (String enchantment : enchantments) {
                    String[] enchantParts = enchantment.split(":");

                    // Ensure the format is correct
                    if (enchantParts.length == 3) {
                        enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);
                        int enchantID = Integer.parseInt(enchantParts[2]);
                    }
                }
            }
        }

        if (block.getType() != Material.ENCHANTING_TABLE) return;

        BlockState state = block.getState();
        if (!(state instanceof TileState tileState)) return;

        PersistentDataContainer container = tileState.getPersistentDataContainer();

        if (container.has(Main.customAltarOfCirceKeys, PersistentDataType.STRING)) {
            String value = container.get(Main.customAltarOfCirceKeys, PersistentDataType.STRING);
            if ("altar_of_circe".equals(value)) {
                // Create an instance of CreateAltarOfCirce and call the method
                CreateAltarOfCirce createAltarOfCirce = new CreateAltarOfCirce(main);

                if (!enchantName.contains("SAFE-MINER")) {
                    // Cancel normal drops
                    event.setDropItems(false);

                    Bukkit.getConsoleSender().sendMessage("Drop2");
                    // Drop the custom altar instead
                    ItemStack customAltar = createAltarOfCirce.CreateAltarOfCirceItem(1, null);
                    block.getWorld().dropItemNaturally(block.getLocation(), customAltar);
                }
            }
        }
    }

}
