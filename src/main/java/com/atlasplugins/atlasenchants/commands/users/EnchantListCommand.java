package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class EnchantListCommand extends AbstractCommand {

    private Main main;
    public EnchantListCommand(Main main) {this.main = main;}

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;

        if(main.getMenusConfig().getBoolean("EnchantList-Gui.RarityList-Menu.Toggle")) {
            main.openEnchantListGUI(player);
        }else {
            if(main.getSettingsConfig().getBoolean("EnchantListMessages.DisabledMenu.Toggle")) {
                // Send UpgradeMenu Disabled Message in chat when called.
                for (String UpgradeMenuDisabledMessage : main.getSettingsConfig().getStringList("EnchantListMessages.DisabledMenu.Message")) {
                    String withPAPISet = main.setPlaceholders((Player) sender, UpgradeMenuDisabledMessage);
                    String message = Main.color(withPAPISet);
                    sender.sendMessage(message);
                }
            }
        }
    }


    @Override
    public void complete(JavaPlugin plugin, CommandSender sender, String label, List<String> args, List<String> completions) {

    }

    @Override
    public List<String> getLabels() {
        return Collections.singletonList("list");
    }

    @Override
    public String getPermission() {
        return "atlasenchants.enchantlist";
    }
}
