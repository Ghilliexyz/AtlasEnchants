package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.ApplyCustomEnchant;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateAltarOfCirce;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateRandomCustomEnchant;
import com.atlasplugins.atlasenchants.managers.ExperienceManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AltarOfCirceEvent implements Listener {

    private static final String ALTAR_TAG = "altar_of_circe";

    private Main main;

    public AltarOfCirceEvent(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e)
    {
        if(e.getRecipe() == null) return;

        boolean isAltarOfCirceCraftingEnabled = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Crafting-Enabled");

        if(!isAltarOfCirceCraftingEnabled) return;

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

    /**
     * Rewrites the three offers shown in the enchanting screen when the block is an Altar Of Circe.
     * Disabled slots are removed entirely, and the enabled ones advertise the Altar's own flat level
     * price so the client shows the real cost and vanilla refuses the click when the player is short.
     */
    @EventHandler
    public void onPrepareEnchant(PrepareItemEnchantEvent e)
    {
        boolean isAltarOfCirceEnabled = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Enabled");

        if(!isAltarOfCirceEnabled) return;

        if(!isAltarOfCirce(e.getEnchantBlock())) return;

        int displayCost = getDisplayCost();
        Enchantment displayEnchantment = getSlotDisplayEnchantment();
        int displayLevel = Math.max(1, main.getEnchantmentsConfig().getInt("AltarOfCirce.AltarOfCirce-Slot-Display-Level", 1));

        EnchantmentOffer[] offers = e.getOffers();

        for (int i = 0; i < offers.length; i++) {
            // A null offer renders as an empty, unclickable slot.
            if (!isSlotEnabled(i + 1)) {
                offers[i] = null;
                continue;
            }

            // Vanilla had nothing to offer for this item, nothing for us to relabel.
            if (offers[i] == null) continue;

            if (displayEnchantment != null) {
                offers[i].setEnchantment(displayEnchantment);
                offers[i].setEnchantmentLevel(displayLevel);
            }

            // The cost must be at least 1 or the client will not let the slot be clicked.
            offers[i].setCost(Math.max(1, displayCost));
        }
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent e)
    {
        boolean isAltarOfCirceEnabled = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Enabled");

        if(!isAltarOfCirceEnabled) return;

        ItemStack item = e.getItem();
        Player player = e.getEnchanter();

        if(!isAltarOfCirce(e.getEnchantBlock())) return;

        // The Altar never uses the vanilla enchanting cost, it charges its own flat level price below.
        // Cancelling also stops vanilla from taking any levels or lapis of its own.
        e.setCancelled(true);

        // Slots the server owner has switched off do nothing at all.
        if (!isSlotEnabled(e.whichButton() + 1)) return;

        boolean chargePlayer = shouldChargePlayer(player);

        if (isChargingLevels()) {
            // --- Experience LEVELS mode (the vanilla-style green number) ---
            int levelCost = chargePlayer ? getLevelCost() : 0;

            if (player.getLevel() < levelCost) {
                handleFeedback(main, player, "AltarOfCirceSounds.NotEnoughLevels", "AltarOfCirceMessages.NotEnoughLevels",
                        "{levelCost}", String.valueOf(levelCost),
                        "{playerLevel}", String.valueOf(player.getLevel()));
                return;
            }

            applyRandomCustomEnchantments(main, player, item, 1, e);

            if (levelCost > 0) {
                player.setLevel(player.getLevel() - levelCost);

                handleFeedback(main, player, "AltarOfCirceSounds.LevelsTaken", "AltarOfCirceMessages.LevelsTaken",
                        "{levelCost}", String.valueOf(levelCost),
                        "{playerLevel}", String.valueOf(player.getLevel()));
            }
        } else {
            // --- Raw XP POINTS mode ---
            int xpCost = chargePlayer ? getXpCost() : 0;
            ExperienceManager xpManager = new ExperienceManager(main);
            int playerXp = xpManager.getExp(player);

            if (playerXp < xpCost) {
                // Reuses the NotEnoughLevels sound; the message is the XP-worded variant.
                handleFeedback(main, player, "AltarOfCirceSounds.NotEnoughLevels", "AltarOfCirceMessages.NotEnoughXP",
                        "{xpCost}", String.valueOf(xpCost),
                        "{playerXP}", String.valueOf(playerXp));
                return;
            }

            applyRandomCustomEnchantments(main, player, item, 1, e);

            if (xpCost > 0) {
                xpManager.changeExp(player, -xpCost);

                // Reuses the LevelsTaken sound; the message is the XP-worded variant.
                handleFeedback(main, player, "AltarOfCirceSounds.LevelsTaken", "AltarOfCirceMessages.XPTaken",
                        "{xpCost}", String.valueOf(xpCost),
                        "{playerXP}", String.valueOf(xpManager.getExp(player)));
            }
        }

        consumeLapis(e);
    }

    /**
     * @return true when the given block is a placed Altar Of Circe rather than a normal enchanting table.
     */
    private static boolean isAltarOfCirce(Block block) {
        if (block == null) return false;

        BlockState state = block.getState();
        if (!(state instanceof TileState tileState)) return false;

        String tag = tileState.getPersistentDataContainer().get(Main.customAltarOfCirceKeys, PersistentDataType.STRING);
        return ALTAR_TAG.equals(tag);
    }

    /**
     * @param slot the slot as the server owner sees it in the config, 1 (top) to 3 (bottom).
     */
    private boolean isSlotEnabled(int slot) {
        return main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Slot-" + slot + "-Enabled", slot == 1);
    }

    private int getLevelCost() {
        return Math.max(0, main.getEnchantmentsConfig().getInt("AltarOfCirce.AltarOfCirce-Enchant-Cost-Levels", 15));
    }

    /**
     * @return true when the Altar charges experience levels, false when it charges raw XP points.
     */
    private boolean isChargingLevels() {
        return main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Cost-Use-Levels", true);
    }

    private int getXpCost() {
        return Math.max(0, main.getEnchantmentsConfig().getInt("AltarOfCirce.AltarOfCirce-Enchant-Cost-XP", 500));
    }

    /**
     * The number rendered on the Altar's enchant slots. The enchanting screen can only show a
     * levels-style price, so in XP mode we display the level a player needs to hold that much XP
     * (rounded down). The real XP charge is still applied on click, backed by the server-side check.
     */
    private int getDisplayCost() {
        if (isChargingLevels()) return getLevelCost();
        return (int) Math.floor(ExperienceManager.getLevelFromExp(getXpCost()));
    }

    private boolean shouldChargePlayer(Player player) {
        if (player.getGameMode() != GameMode.CREATIVE) return true;
        return main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Charge-Creative-Players", false);
    }

    /**
     * Resolves the vanilla enchantment used as the label on the Altar's slots. Minecraft can only render
     * real enchantments there, so this cannot be arbitrary text.
     *
     * @return the configured enchantment, or null to leave vanilla's own rolled label alone.
     */
    private Enchantment getSlotDisplayEnchantment() {
        String name = main.getEnchantmentsConfig().getString("AltarOfCirce.AltarOfCirce-Slot-Display-Enchantment", "LURE");

        if (name == null || name.isBlank() || name.equalsIgnoreCase("NONE")) return null;

        Enchantment enchantment = Registry.ENCHANTMENT.match(name);

        if (enchantment == null) {
            main.getLogger().warning("Invalid AltarOfCirce-Slot-Display-Enchantment '" + name + "'. Leaving the vanilla slot label alone.");
        }

        return enchantment;
    }

    /**
     * Cancelling the enchant leaves the lapis in the table, so take it by hand to keep the vanilla feel.
     */
    private void consumeLapis(EnchantItemEvent e) {
        if (!main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Consume-Lapis", true)) return;

        ItemStack lapis = e.getView().getItem(1);
        if (lapis == null || lapis.getType() == Material.AIR) return;

        // Take a flat configured amount rather than vanilla's slot-based 1/2/3, capped at what's there.
        int amountToTake = Math.max(0, main.getEnchantmentsConfig().getInt("AltarOfCirce.AltarOfCirce-Lapis-Cost", 3));
        if (amountToTake == 0) return;

        if (lapis.getAmount() <= amountToTake) {
            e.getView().setItem(1, null);
        } else {
            lapis.setAmount(lapis.getAmount() - amountToTake);
        }

        e.getEnchanter().updateInventory();
    }

    /**
     * Plays the configured sound and sends the configured message for the given config paths.
     *
     * @param soundConfigPath The base path for sound settings in config (e.g., "AltarOfCirceSounds.NotEnoughLevels").
     * @param messageConfigPath The base path for message settings in config (e.g., "AltarOfCirceMessages.NotEnoughLevels").
     * @param placeholders Key-value pairs of placeholders to replace in messages (e.g., "{levelCost}", "15").
     */
    private static void handleFeedback(Main main, Player player, String soundConfigPath, String messageConfigPath, String... placeholders) {
        boolean playSound = main.getSettingsConfig().getBoolean(soundConfigPath + ".Toggle");
        if (playSound) {
            try {
                Sound sound = Main.getSound(main.getSettingsConfig().getString(soundConfigPath + ".Sound"));
                float volume = (float) main.getSettingsConfig().getDouble(soundConfigPath + ".Volume");
                float pitch = (float) main.getSettingsConfig().getDouble(soundConfigPath + ".Pitch");
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException ex) {
                main.getLogger().warning("Invalid sound specified in config path: " + soundConfigPath + ".Sound. Error: " + ex.getMessage());
            }
        }

        boolean sendMessage = main.getSettingsConfig().getBoolean(messageConfigPath + ".Toggle");
        if (sendMessage) {
            for (String msg : main.getSettingsConfig().getStringList(messageConfigPath + ".Message")) {
                String processedMsg = main.setPlaceholders(player, msg);
                for (int i = 0; i < placeholders.length; i += 2) {
                    if (i + 1 < placeholders.length) {
                        processedMsg = processedMsg.replace(placeholders[i], placeholders[i + 1]);
                    }
                }
                player.sendMessage(Main.color(processedMsg));
            }
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

        boolean isBook = targetItem.getType() == Material.BOOK;

        boolean canEnchantToolsAndArmour = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-ArmourTools-Enchanter-Enabled");

        if(canEnchantToolsAndArmour && !isBook){
            ApplyToArmourAndTools(main, player, targetItem, numberOfEnchantmentsToApply);
        }

        boolean canGetVanillaEnchantments = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Allow-Vanilla-ToolsAndArmour-Enchants-Enabled");

        // The event is always cancelled so the Altar can charge its own price, which also throws away
        // vanilla's roll. Re-apply that roll by hand when the server owner wants vanilla enchants too.
        if(canGetVanillaEnchantments && !isBook && e != null){
            for (Map.Entry<Enchantment, Integer> vanillaEnchant : e.getEnchantsToAdd().entrySet()) {
                targetItem.addUnsafeEnchantment(vanillaEnchant.getKey(), vanillaEnchant.getValue());
            }
        }

        boolean canEnchantBook = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Book-Enchanter-Enabled");

        if(canEnchantBook && isBook){
            ApplyToBook(main, player, targetItem, e);
        }
    }

    public static  void ApplyToBook(Main main, Player player, ItemStack targetItem, EnchantItemEvent e)
    {
        // Basic validation: ensure we have a valid item and a positive number of enchantments to apply.
        if (targetItem == null || targetItem.getType() == Material.AIR) {
            return;
        }

        // The event is cancelled so vanilla leaves the book in the table. Clearing the ItemStack handed to us
        // by the event does nothing to the real slot, so empty the table's item slot directly.
        consumeBook(player, e);

        // Create an instance of CreateRandomCustomEnchant and call the method
        CreateRandomCustomEnchant createRandomCustomEnchant = new CreateRandomCustomEnchant(main);
        createRandomCustomEnchant.CreateRandomOracleEnchantmentItem(player, 1, true, null);
    }

    /**
     * Removes one book from the Altar's item slot (slot 0 of the enchanting view).
     */
    private static void consumeBook(Player player, EnchantItemEvent e) {
        if (e == null) return;

        ItemStack slotItem = e.getView().getItem(0);
        if (slotItem == null || slotItem.getType() == Material.AIR) return;

        if (slotItem.getAmount() <= 1) {
            e.getView().setItem(0, null);
        } else {
            slotItem.setAmount(slotItem.getAmount() - 1);
        }

        player.updateInventory();
    }

    public static void ApplyToArmourAndTools(Main main, Player player, ItemStack targetItem, int numberOfEnchantmentsToApply)
    {
        // Basic validation: ensure we have a valid item and a positive number of enchantments to apply.
        if (targetItem == null || targetItem.getType() == Material.AIR || numberOfEnchantmentsToApply <= 0 || targetItem.getType() == Material.BOOK) {
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
                allAvailableEnchantNames.remove(randomIndex); // Remove from the pool for this batch
                i--; // Decrement counter to try for another enchantment
                continue;
            }

            // Check if the chosen enchantment is enabled.
            boolean isEnchantmentEnabled = chosenEnchantConfig.getBoolean("Enchantment-Enabled", false);
            if (!isEnchantmentEnabled) {
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

            main.debugOddsInfo("Altar", player.getName(), (resultOfApplication != null ? "Applied " : "Failed to apply ")
                    + chosenEnchantName + " (Lvl " + randomLevel + "/" + maxLevel + ") to " + targetItem.getType()
                    + " - picked from " + allAvailableEnchantNames.size() + " remaining enchant(s).");

            if (resultOfApplication != null) {
                // The enchantment was successfully applied or upgraded.
                successfullyAppliedCount++; // Increment the count of successfully applied *distinct* enchants.
                // Remove the enchantment from the pool for this batch to ensure distinct enchantments are attempted.
                allAvailableEnchantNames.remove(randomIndex);
            } else {
                // The enchantment could not be applied (e.g., due to a blacklist conflict, already max level, or wrong item type).
                allAvailableEnchantNames.remove(randomIndex); // Remove it from the pool to avoid re-trying the same failing enchant in this batch.
                i--; // Decrement counter to ensure we still attempt to apply 'numberOfEnchantmentsToApply' distinct enchants.
            }
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
            if (enchantedItemData != null && !enchantedItemData.isEmpty()) {
                for (String enchantment : enchantedItemData.split(",")) {
                    String[] enchantParts = enchantment.split(":");

                    // Ensure the format is correct. Only the name is needed here; the level and
                    // ID in the remaining parts are not used by this check.
                    if (enchantParts.length == 3) {
                        enchantName = enchantParts[0];
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

                    // this is also where the player will be allowed to mine it with any tool including their fist

                    // Drop the custom altar instead
                    ItemStack customAltar = createAltarOfCirce.CreateAltarOfCirceItem(1, null);
                    block.getWorld().dropItemNaturally(block.getLocation(), customAltar);
                }
            }
        }
    }

}
