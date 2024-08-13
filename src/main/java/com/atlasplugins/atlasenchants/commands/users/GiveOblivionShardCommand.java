package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateShard;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class GiveOblivionShardCommand extends AbstractCommand {

    private final Main main;
    public GiveOblivionShardCommand(Main main) {this.main = main;}

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {
        if (args.size() < 2) {
            // Send Usage Message in chat when called.
            for (String UsageMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveOblivionShard-Usage-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, UsageMessage);
                String message = Main.color(withPAPISet);
                sender.sendMessage(message);
            }
            return;
        }

        Player player = Bukkit.getPlayer(args.get(0));
        if (player == null) {
            // Send PlayerNotFound Message in chat when called.
            for (String PlayerNotFoundMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveOblivionShard-PlayerNotFound-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, PlayerNotFoundMessage);
                String message = Main.color(withPAPISet);
                sender.sendMessage(message);
            }
            return;
        }

        int shardAmount;
        try {
            shardAmount = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            // Send InvalidAmount Message in chat when called.
            for (String InvalidAmountMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveOblivionShard-InvalidAmount-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, InvalidAmountMessage);
                String message = Main.color(withPAPISet);
                sender.sendMessage(message);
            }
            return;
        }

        if (shardAmount < 1 || shardAmount > 64) {
            // Send AmountRange Message in chat when called.
            for (String AmountRangeMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveOblivionShard-AmountRange-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, AmountRangeMessage);
                String message = Main.color(withPAPISet);
                sender.sendMessage(message);
            }
            return;
        }

        // Create an instance of CreateCustomEnchant and call the method
        CreateShard createShard = new CreateShard(main);
        createShard.CreateShardItem(shardAmount, player);
    }

    @Override
    public void complete(JavaPlugin plugin, CommandSender sender, String label, List<String> args, List<String> completions) {
        if (args.size() == 1) { // Tab completing the first argument (player name)
            String input = args.get(0).toLowerCase(); // Current input typed by the player
            Bukkit.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .forEach(completions::add);
        } else if (args.size() == 2) { // Tab completing the second argument (amount)
            completions.add("[1-64]");
        }
    }

    @Override
    public List<String> getLabels() {
        return Collections.singletonList("giveshard");
    }

    @Override
    public String getPermission() {
        return "atlasenchants.giveshard";
    }
}
