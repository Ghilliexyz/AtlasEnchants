package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCustomEnchant;
import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GiveEnchantCommand extends AbstractCommand {

    private final Main main;
    public GiveEnchantCommand(Main main) {this.main = main;}

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {
        if (args.size() < 4) {
            // Send Usage Message in chat when called.
            for (String UsageMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveEnchant-Usage-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, UsageMessage);
                String message = Main.color(withPAPISet);
                sender.sendMessage(message);
            }
            return;
        }

        Player player = Bukkit.getPlayer(args.get(0));
        if (player == null) {
            // Send PlayerNotFound Message in chat when called.
            for (String PlayerNotFoundMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveEnchant-PlayerNotFound-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, PlayerNotFoundMessage);
                String message = Main.color(withPAPISet);
                sender.sendMessage(message);
            }
            return;
        }

        String enchantmentName = args.get(1).toUpperCase();
        if (!main.getEnchantmentsConfig().contains("Enchantments." + enchantmentName)) {
            // Send EnchantmentNotFound Message in chat when called.
            for (String EnchantmentNotFoundMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveEnchant-EnchantmentNotFound-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, EnchantmentNotFoundMessage);
                String message = Main.color(withPAPISet);
                sender.sendMessage(message);
            }
            return;
        }

        int enchantmentLevel;
        int enchantMaxLvl = main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Enchantment-MaxLvl");
        try {
            enchantmentLevel = Integer.parseInt(args.get(2));
        } catch (NumberFormatException e) {
            // Send InvalidLevel Message in chat when called.
            for (String InvalidLevelMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveEnchant-InvalidLevel-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, InvalidLevelMessage);
                String message = Main.color(withPAPISet)
                        .replace("{enchantMaxLvl}", String.valueOf(enchantMaxLvl));
                sender.sendMessage(message);
            }
            return;
        }

        if (enchantmentLevel < 1 || enchantmentLevel > enchantMaxLvl) {
            // Send LevelRange Message in chat when called.
            for (String LevelRangeMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveEnchant-LevelRange-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, LevelRangeMessage);
                String message = Main.color(withPAPISet)
                        .replace("{enchantMaxLvl}", String.valueOf(enchantMaxLvl));
                sender.sendMessage(message);
            }
            return;
        }

        int enchantmentAmount;
        try {
            enchantmentAmount = Integer.parseInt(args.get(3));
        } catch (NumberFormatException e) {
            // Send InvalidAmount Message in chat when called.
            for (String InvalidAmountMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveEnchant-InvalidAmount-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, InvalidAmountMessage);
                String message = Main.color(withPAPISet);
                sender.sendMessage(message);
            }
            return;
        }

        if (enchantmentAmount < 1 || enchantmentAmount > 64) {
            // Send AmountRange Message in chat when called.
            for (String AmountRangeMessage : main.getSettingsConfig().getStringList("Command-Messages.Command-Messages-GiveEnchant-AmountRange-Message")) {
                String withPAPISet = main.setPlaceholders((Player) sender, AmountRangeMessage);
                String message = Main.color(withPAPISet);
                sender.sendMessage(message);
            }
            return;
        }

        // Create an instance of CreateCustomEnchant and call the method
        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
        createCustomEnchant.CreateCustomEnchantmentItem(enchantmentName, enchantmentLevel, enchantmentAmount, player);
    }

    @Override
    public void complete(JavaPlugin plugin, CommandSender sender, String label, List<String> args, List<String> completions) {
        if (args.size() == 1) { // Tab completing the first argument (player name)
            String input = args.get(0).toLowerCase(); // Current input typed by the player
            Bukkit.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .forEach(completions::add);
        } else if (args.size() == 2) { // Tab completing the second argument (enchant name)
            String input = args.get(1).toUpperCase(); // Current input typed by the player
            List<String> enchantments = main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false).stream()
                    .map(String::toUpperCase)
                    .filter(name -> name.contains(input))
                    .collect(Collectors.toList());
            completions.addAll(enchantments);
        } else if (args.size() == 3) { // Tab completing the third argument (level)
            String enchantName = args.get(1).toUpperCase();
            if (main.getEnchantmentsConfig().contains("Enchantments." + enchantName)) {
                int maxLevel = main.getEnchantmentsConfig().getInt("Enchantments." + enchantName + ".Enchantment-MaxLvl");
                for (int i = 1; i <= maxLevel; i++) {
                    completions.add(String.valueOf(i));
                }
            }
        } else if (args.size() == 4) { // Tab completing the fourth argument (amount)
            completions.add("[1-64]");
        }
    }

    @Override
    public List<String> getLabels() {
        return Collections.singletonList("giveenchant");
    }

    @Override
    public String getPermission() {
        return "atlasenchants.giveenchant";
    }
}
