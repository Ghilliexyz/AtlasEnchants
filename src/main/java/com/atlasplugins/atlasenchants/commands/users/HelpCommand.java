package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import com.atlasplugins.atlasenchants.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class HelpCommand extends AbstractCommand {

    private Main main;
    public HelpCommand(Main main) {this.main = main;}

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {
        // if all checks, check out then move on to the command
        sender.sendMessage(Main.color("&c&m&l------------&f&l [&x&F&F&3&C&3&C&lA&x&F&F&4&C&3&E&lt&x&F&E&5&C&4&0&ll&x&F&E&6&C&4&2&la&x&F&E&7&C&4&4&ls &x&F&D&8&C&4&6&lE&x&F&D&9&C&4&8&ln&x&F&D&A&B&4&A&lc&x&F&C&B&B&4&C&lh&x&F&C&C&B&4&E&la&x&F&C&D&B&5&0&ln&x&F&B&E&B&5&2&lt&x&F&B&F&B&5&4&ls&f&l] &c&m&l-------------"));
        sender.sendMessage(Main.color(""));
        sender.sendMessage(Main.color("&c● &7Reload command: &c/aenchants reload"));
        sender.sendMessage(Main.color("&c● &7reloads the Atlas Enchants configs"));
        sender.sendMessage(Main.color(""));
        sender.sendMessage(Main.color("&c● &7Give Enchant command: &c/aenchants give &e<PlayerName> <EnchantName> <EnchantLevel> <Amount>"));
        sender.sendMessage(Main.color("&c● &7Gives the named user a Custom Enchant"));
        sender.sendMessage(Main.color(""));
        sender.sendMessage(Main.color("&c&m&l-----------------------------------------"));
    }

    @Override
    public void complete(JavaPlugin plugin, CommandSender sender, String label, List<String> args, List<String> completions) {

    }

    @Override
    public List<String> getLabels() {
        return Collections.singletonList("help");
    }

    @Override
    public String getPermission() {
        return "atlasenchants.help";  // permission required for help command
    }
}


