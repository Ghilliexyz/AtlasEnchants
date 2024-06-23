package com.atlasplugins.atlasenchants.Commands;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadConfigsCommand implements CommandExecutor {

    private Main main;
    public ReloadConfigsCommand(Main main) {this.main = main;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {

        String permissiongString = main.getSettingsConfig().getString("EnchantItems.EnchantItem-ReloadConfig-Command-Permission");
        if(permissiongString == null) {return true;}
        if(!sender.hasPermission(permissiongString) || !sender.isOp())
        {
            // Send NoPermissions Message in chat when called.
            for (String NoPermMessage : main.getSettingsConfig().getStringList("EnchantItem-ReloadConfig-Messages.EnchantItem-ReloadConfig-NoPermissions-Message")) {
                String message = Main.color(NoPermMessage);
                sender.sendMessage(message);
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            // Send NotAPlayer Message in chat when called.
            for (String NotAPlayerMessage : main.getSettingsConfig().getStringList("EnchantItem-ReloadConfig-Messages.EnchantItem-ReloadConfig-NotAPlayer-Message")) {
                String message = Main.color(NotAPlayerMessage);
                sender.sendMessage(message);
            }
            return true;
        }

        // Reload Settings config
        main.loadSettingsConfig();

        // Reload Enchantments config
        main.loadEnchantmentsConfig();

        // Send ConfigReloaded Message in chat when called.
        for (String ConfigReloadedMessage : main.getSettingsConfig().getStringList("EnchantItem-ReloadConfig-Messages.EnchantItem-ReloadConfig-ConfigReloaded-Message")) {
            String message = Main.color(ConfigReloadedMessage);
            sender.sendMessage(message);
        }

        return false;
    }
}
