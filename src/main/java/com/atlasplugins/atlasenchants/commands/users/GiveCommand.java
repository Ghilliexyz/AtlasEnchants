package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GiveCommand extends AbstractCommand {

    private final Main main;
    private final Map<String, AbstractCommand> subcommands = new LinkedHashMap<>();

    public GiveCommand(Main main) {
        this.main = main;
        subcommands.put("enchant", new GiveEnchantCommand(main));
        subcommands.put("random", new GiveRandomEnchantCommand(main));
        subcommands.put("shard", new GiveOblivionShardCommand(main));
        subcommands.put("oracle", new GiveOracleOfEnchantmentCommand(main));
        subcommands.put("altar", new GiveAltarOfCirceCommand(main));
        subcommands.put("scrap", new GiveScrapOfCirceCommand(main));
        subcommands.put("ember", new GiveCircesEmberCommand(main));
        subcommands.put("anvil", new GiveCircesAnvilCommand(main));
    }

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage(Main.color("&cUsage: /ae give <" + String.join("|", subcommands.keySet()) + "> ..."));
            return;
        }

        String subLabel = args.get(0).toLowerCase(Locale.ROOT);
        AbstractCommand sub = subcommands.get(subLabel);

        if (sub == null) {
            sender.sendMessage(Main.color("&cUnknown item type: &e" + args.get(0) + "&c. Available: &e" + String.join(", ", subcommands.keySet())));
            return;
        }

        // Check the subcommand's permission
        String permission = sub.getPermission();
        if (permission != null && !permission.isEmpty()) {
            if (!sender.hasPermission(permission) && !sender.isOp()) {
                for (String noPermission : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-NoPermissions")) {
                    if (sender instanceof Player) {
                        String withPAPISet = main.setPlaceholders((Player) sender, noPermission);
                        sender.sendMessage(Main.color(withPAPISet));
                    } else {
                        sender.sendMessage(Main.color(noPermission));
                    }
                }
                return;
            }
        }

        // Delegate to subcommand with remaining args
        sub.execute(plugin, sender, subLabel, args.subList(1, args.size()));
    }

    @Override
    public void complete(JavaPlugin plugin, CommandSender sender, String label, List<String> args, List<String> completions) {
        if (args.size() == 1) {
            String input = args.get(0).toLowerCase(Locale.ROOT);
            subcommands.keySet().stream()
                    .filter(name -> name.startsWith(input))
                    .forEach(completions::add);
        } else if (args.size() > 1) {
            String subLabel = args.get(0).toLowerCase(Locale.ROOT);
            AbstractCommand sub = subcommands.get(subLabel);
            if (sub != null) {
                sub.complete(plugin, sender, subLabel, args.subList(1, args.size()), completions);
            }
        }
    }

    @Override
    public List<String> getLabels() {
        return Collections.singletonList("give");
    }

    @Override
    public String getPermission() {
        return null; // Permission is checked per-subcommand
    }
}
