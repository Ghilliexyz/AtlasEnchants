package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class UpgradeCommand extends AbstractCommand {

    private Main main;
    public UpgradeCommand(Main main) {this.main = main;}

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;

        if(main.getSettingsConfig().getBoolean("UpgradeEnchant-Gui.UpgradeEnchant-Menu.UpgradeEnchant-Menu-Toggle"))
        {
            main.openUpgradeEnchantGUI(player);
        }else {
            if(main.getSettingsConfig().getBoolean("UpgradeEnchantMessages.UpgradeEnchant-DisabledMenu-Message-Toggle")) {
                // Send UpgradeMenu Disabled Message in chat when called.
                for (String UpgradeMenuDisabledMessage : main.getSettingsConfig().getStringList("UpgradeEnchantMessages.UpgradeEnchant-DisabledMenu-Message")) {
                    String withPAPISet = main.setPlaceholders((Player) sender, UpgradeMenuDisabledMessage);
                    String message = Main.color(withPAPISet);
                    sender.sendMessage(message);
                }
            }

            // Get the bool to check if the user wants to play the blacklisted enchant sound
            boolean UpgradeMenuDisabledPlaySound = main.getSettingsConfig().getBoolean("UpgradeEnchantSounds.UpgradeEnchant-DisabledMenu-Sound-Toggle");
            // check if the user wants to play the Already Applied sound
            if(UpgradeMenuDisabledPlaySound){
                // Get apply sound via config.
                Sound upgradeMenuDisabledSound = Sound.valueOf(main.getSettingsConfig().getString("UpgradeEnchantSounds.UpgradeEnchant-DisabledMenu-Sound"));
                float upgradeMenuDisabledVolume = main.getSettingsConfig().getInt("UpgradeEnchantSounds.UpgradeEnchant-DisabledMenu-Volume");
                float upgradeMenuDisabledPitch = main.getSettingsConfig().getInt("UpgradeEnchantSounds.UpgradeEnchant-DisabledMenu-Pitch");

                // Play sound for when enchant is Already Applied.
                player.playSound(player.getLocation(), upgradeMenuDisabledSound, upgradeMenuDisabledVolume, upgradeMenuDisabledPitch);
            }
        }
    }

    @Override
    public void complete(JavaPlugin plugin, CommandSender sender, String label, List<String> args, List<String> completions) {

    }

    @Override
    public List<String> getLabels() {
        return Collections.singletonList("upgrade");
    }

    @Override
    public String getPermission() {
        return "atlasenchants.upgrade";  // permission required for help command
    }
}


