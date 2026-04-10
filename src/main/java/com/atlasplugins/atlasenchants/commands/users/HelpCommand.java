package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import com.atlasplugins.atlasenchants.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class HelpCommand extends AbstractCommand {

    private Main main;
    public HelpCommand(Main main) {this.main = main;}

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {
        // Player commands - always shown
        for (String line : main.getSettingsConfig().getStringList("Help-Messages.Player-Help")) {
            if (sender instanceof Player) {
                String withPAPISet = main.setPlaceholders((Player) sender, line);
                sender.sendMessage(Main.color(withPAPISet));
            } else {
                sender.sendMessage(Main.color(line));
            }
        }

        // Admin commands - only shown if sender has atlasenchants.help or is OP
        if (sender.hasPermission("atlasenchants.help") || sender.isOp()) {
            for (String line : main.getSettingsConfig().getStringList("Help-Messages.Admin-Help")) {
                if (sender instanceof Player) {
                    String withPAPISet = main.setPlaceholders((Player) sender, line);
                    sender.sendMessage(Main.color(withPAPISet));
                } else {
                    sender.sendMessage(Main.color(line));
                }
            }
        }
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
        return null;
    }

    @Override
    public boolean requiresPlayer() { return false; }
}


