package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCircesBrand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class GiveCircesBrandCommand extends AbstractCommand {

    private final Main main;
    public GiveCircesBrandCommand(Main main) {this.main = main;}

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {
        if (args.size() < 2) {
            sendConfigMessages(sender, "Command-Messages.Command-Messages-CircesBrand-Usage-Message");
            return;
        }

        Player player = Bukkit.getPlayer(args.get(0));
        if (player == null) {
            sendConfigMessages(sender, "Command-Messages.Command-Messages-CircesBrand-PlayerNotFound-Message");
            return;
        }

        int brandAmount;
        try {
            brandAmount = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            sendConfigMessages(sender, "Command-Messages.Command-Messages-CircesBrand-InvalidAmount-Message");
            return;
        }

        if (brandAmount < 1 || brandAmount > 64) {
            sendConfigMessages(sender, "Command-Messages.Command-Messages-CircesBrand-AmountRange-Message");
            return;
        }

        CreateCircesBrand createCircesBrand = new CreateCircesBrand(main);
        createCircesBrand.CreateCircesBrandItem(brandAmount, player);
    }

    // The console has no PlaceholderAPI context, so only run placeholders for players.
    private void sendConfigMessages(CommandSender sender, String path) {
        for (String line : main.getSettingsConfig().getStringList(path)) {
            if (sender instanceof Player p) {
                line = main.setPlaceholders(p, line);
            }
            sender.sendMessage(Main.color(line));
        }
    }

    @Override
    public void complete(JavaPlugin plugin, CommandSender sender, String label, List<String> args, List<String> completions) {
        if (args.size() == 1) {
            String input = args.get(0).toLowerCase();
            Bukkit.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .forEach(completions::add);
        } else if (args.size() == 2) {
            completions.add("[1-64]");
        }
    }

    @Override
    public List<String> getLabels() {
        return Collections.singletonList("givecircesbrand");
    }

    @Override
    public String getPermission() {
        return "atlasenchants.givecircesbrand";
    }
}
