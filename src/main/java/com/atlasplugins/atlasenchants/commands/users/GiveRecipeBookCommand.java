package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateRecipeBook;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class GiveRecipeBookCommand extends AbstractCommand {

    private final Main main;
    public GiveRecipeBookCommand(Main main) {this.main = main;}

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {
        if (args.size() < 2) {
            for (String UsageMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-CircesGrimoire-Usage-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, UsageMessage);
                sender.sendMessage(Main.color(withPAPISet));
            }
            return;
        }

        Player player = Bukkit.getPlayer(args.get(0));
        if (player == null) {
            for (String PlayerNotFoundMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-CircesGrimoire-PlayerNotFound-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, PlayerNotFoundMessage);
                sender.sendMessage(Main.color(withPAPISet));
            }
            return;
        }

        int bookAmount;
        try {
            bookAmount = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            for (String InvalidAmountMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-CircesGrimoire-InvalidAmount-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, InvalidAmountMessage);
                sender.sendMessage(Main.color(withPAPISet));
            }
            return;
        }

        if (bookAmount < 1 || bookAmount > 64) {
            for (String AmountRangeMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-CircesGrimoire-AmountRange-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, AmountRangeMessage);
                sender.sendMessage(Main.color(withPAPISet));
            }
            return;
        }

        CreateRecipeBook createRecipeBook = new CreateRecipeBook(main);
        createRecipeBook.createRecipeBook(bookAmount, player);
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
        return Collections.singletonList("giverecipebook");
    }

    @Override
    public String getPermission() {
        return "atlasenchants.givegrimoire";
    }
}
