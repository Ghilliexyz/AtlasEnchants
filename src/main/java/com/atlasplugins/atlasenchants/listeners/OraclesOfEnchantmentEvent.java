package com.atlasplugins.atlasenchants.listeners;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

// this handles the interaction between the player and the GUI
// to open the Enchant list GUI
public class OraclesOfEnchantmentEvent implements Listener {

    private Main main;

    public OraclesOfEnchantmentEvent(Main main) {
        this.main = main    ;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        Player player = (Player) e.getPlayer();

        Action action = e.getAction();

        Block block = e.getClickedBlock();

        Material correctBlock = Material.LECTERN;

        ItemStack itemInHand = e.getItem();

        Material oracleItem = Material.valueOf(main.getEnchantmentsConfig().getString("OraclesOfEnchantment.OraclesOfEnchantment-Item"));

        if (itemInHand != null) {
            if(action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
            {
                if(itemInHand.getType() == oracleItem){
                    if(!itemInHand.hasItemMeta()) return;
                    ItemMeta bookMeta = itemInHand.getItemMeta();
                    PersistentDataContainer bookPDC = bookMeta.getPersistentDataContainer();

                    if(bookPDC.has(Main.customOracleKeys, PersistentDataType.STRING)) {
                        String bookData = bookPDC.get(Main.customOracleKeys, PersistentDataType.STRING);

                        if (bookData == null) return;

                        String oracleName = bookData;

                        if(oracleName.equals("Oracle")){
                            if(block == null || block.getType() != correctBlock)
                            {
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }

        if(action == Action.RIGHT_CLICK_BLOCK){

            if(block.getType() != correctBlock) return;
            if(getLecternBook(block) == null) return;
            if(!getLecternBook(block).hasItemMeta()) return;

            ItemMeta blockMeta = getLecternBook(block).getItemMeta();

            PersistentDataContainer blockPDC = blockMeta.getPersistentDataContainer();

            if(blockPDC.has(Main.customOracleKeys, PersistentDataType.STRING))
            {
                String blockData = blockPDC.get(Main.customOracleKeys, PersistentDataType.STRING);

                if(blockData == null) return;

                // Split the oracle data into name and level
                String oracleName = blockData;

                if(action == Action.RIGHT_CLICK_BLOCK)
                {
                    boolean isOracleEnabled = main.getEnchantmentsConfig().getBoolean("OraclesOfEnchantment.OraclesOfEnchantment-Enabled");

                    if(!isOracleEnabled){
                        // Get oracle Disabled sound via config.
                        Sound oracleDisabledSound = Sound.valueOf(main.getSettingsConfig().getString("OracleItemSounds.OracleItem-DisabledOracle-Sound"));
                        float oracleDisabledVolume = main.getSettingsConfig().getInt("OracleItemSounds.OracleItem-DisabledOracle-Volume");
                        float oracleDisabledPitch = main.getSettingsConfig().getInt("OracleItemSounds.OracleItem-DisabledOracle-Pitch");

                        // Get the bool to check if the user wants to play the oracle Disabled sound
                        boolean oracleDisabledPlaySound = main.getSettingsConfig().getBoolean("OracleItemSounds.OracleItem-DisabledOracle-Sound-Toggle");

                        // check if the user doesn't want to play the sound then return if not.
                        if(oracleDisabledPlaySound) {
                            // Play sound for when enchant is blacklisted.
                            player.playSound(player.getLocation(), oracleDisabledSound, oracleDisabledVolume, oracleDisabledPitch);
                        }

                        // Get the bool to check if the user wants to show the oracle Disabled message
                        boolean oracleDisabledSendMessage = main.getSettingsConfig().getBoolean("OracleItemMessages.OracleItem-DisabledOracle-Message-Toggle");
                        // check if the user doesn't want to send the oracle Disabled Message, return if not.
                        if (oracleDisabledSendMessage) {
                            // Send blacklisted Message in chat
                            for (String BlacklistMessage : main.getSettingsConfig().getStringList("OracleItemMessages.OracleItem-DisabledOracle-Message")) {
                                String withPAPISet1 = main.setPlaceholders(player, BlacklistMessage);
                                String message = Main.color(withPAPISet1)
                                        .replace("{oracleName}", formatOracleName(oracleName));
        //                                .replace("{oracleID}", String.valueOf(oracleID));
                                player.sendMessage(message);
                            }
                        }
                        return;
                    }

                    // Get oracle Opening sound via config.
                    Sound oracleDisabledSound = Sound.valueOf(main.getSettingsConfig().getString("OracleItemSounds.OracleItem-Opening-Sound"));
                    float oracleDisabledVolume = main.getSettingsConfig().getInt("OracleItemSounds.OracleItem-Opening-Volume");
                    float oracleDisabledPitch = main.getSettingsConfig().getInt("OracleItemSounds.OracleItem-Opening-Pitch");

                    // Get the bool to check if the user wants to play the oracle Disabled sound
                    boolean oracleDisabledPlaySound = main.getSettingsConfig().getBoolean("OracleItemSounds.OracleItem-Opening-Sound-Toggle");

                    // check if the user doesn't want to play the sound then return if not.
                    if(oracleDisabledPlaySound) {
                        // Play sound for when enchant is blacklisted.
                        player.playSound(player.getLocation(), oracleDisabledSound, oracleDisabledVolume, oracleDisabledPitch);
                    }

                    // Get the bool to check if the user wants to show the oracle Disabled message
                    boolean oracleDisabledSendMessage = main.getSettingsConfig().getBoolean("OracleItemMessages.OracleItem-Opening-Message-Toggle");
                    // check if the user doesn't want to send the oracle Disabled Message, return if not.
                    if (oracleDisabledSendMessage) {
                        // Send blacklisted Message in chat
                        for (String BlacklistMessage : main.getSettingsConfig().getStringList("OracleItemMessages.OracleItem-Opening-Message")) {
                            String withPAPISet1 = main.setPlaceholders(player, BlacklistMessage);
                            String message = Main.color(withPAPISet1)
                                    .replace("{oracleName}", formatOracleName(oracleName));
        //                            .replace("{oracleID}", String.valueOf(oracleID));
                            player.sendMessage(message);
                        }
                    }

                    if(oracleName.equals("Oracle")){
                        e.setCancelled(true);
                        main.openEnchantListGUI(player);
                    }
                }
            }

        }
    }

    public ItemStack getLecternBook(Block lecternBlock){
        ItemStack book = null;
        if(lecternBlock.getState() instanceof Lectern)
        {
            Lectern lectern = (Lectern) lecternBlock.getState();
            book = lectern.getInventory().getItem(0);
        }
        return book;
    }

    private String formatOracleName(String oracleName) {
        if(oracleName == null){
            return "";
        }
        // Replace periods with spaces
        String formattedName = oracleName.replace('-', ' ');

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
