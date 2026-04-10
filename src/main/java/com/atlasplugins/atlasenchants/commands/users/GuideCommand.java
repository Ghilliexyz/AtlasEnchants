package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class GuideCommand extends AbstractCommand {

    private Main main;
    public GuideCommand(Main main) {this.main = main;}

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;
        main.openGuideGUI(player);
    }

    @Override
    public void complete(JavaPlugin plugin, CommandSender sender, String label, List<String> args, List<String> completions) {

    }

    @Override
    public List<String> getLabels() {
        return Collections.singletonList("guide");
    }

    @Override
    public String getPermission() {
        return "atlasenchants.guide";
    }
}
