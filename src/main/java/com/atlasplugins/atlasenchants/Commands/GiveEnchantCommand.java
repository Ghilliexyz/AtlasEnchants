package com.atlasplugins.atlasenchants.Commands;

import com.atlasplugins.atlasenchants.Listeners.CreateCustomEnchant;
import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GiveEnchantCommand implements CommandExecutor, TabCompleter {

    private Main main;
    public GiveEnchantCommand(Main main) {this.main = main;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String permissionString = main.getSettingsConfig().getString("EnchantItems.EnchantItem-GiveEnchant-Command-Permission");
        if(permissionString == null) {return true;}
        if(!sender.hasPermission(permissionString) || !sender.isOp())
        {
            // Send NoPermissions Message in chat when called.
            for (String NoPermMessage : main.getSettingsConfig().getStringList("EnchantItem-GiveEnchant-Messages.EnchantItem-GiveEnchant-NoPermissions-Message")) {
                String message = Main.color(NoPermMessage);
                sender.sendMessage(message);
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            // Send NotAPlayer Message in chat when called.
            for (String NotAPlayerMessage : main.getSettingsConfig().getStringList("EnchantItem-GiveEnchant-Messages.EnchantItem-GiveEnchant-NotAPlayer-Message")) {
                String message = Main.color(NotAPlayerMessage);
                sender.sendMessage(message);
            }
            return true;
        }

        if (args.length < 4) {
            // Send Usage Message in chat when called.
            for (String UsageMessage : main.getSettingsConfig().getStringList("EnchantItem-GiveEnchant-Messages.EnchantItem-GiveEnchant-Usage-Message")) {
                String message = Main.color(UsageMessage);
                sender.sendMessage(message);
            }
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            // Send PlayerNotFound Message in chat when called.
            for (String PlayerNotFoundMessage : main.getSettingsConfig().getStringList("EnchantItem-GiveEnchant-Messages.EnchantItem-GiveEnchant-PlayerNotFound-Message")) {
                String message = Main.color(PlayerNotFoundMessage);
                sender.sendMessage(message);
            }
            return true;
        }

        String enchantmentName = args[1].toUpperCase();
        if (!main.getEnchantmentsConfig().contains("Enchantments." + enchantmentName)) {
            // Send EnchantmentNotFound Message in chat when called.
            for (String EnchantmentNotFoundMessage : main.getSettingsConfig().getStringList("EnchantItem-GiveEnchant-Messages.EnchantItem-GiveEnchant-EnchantmentNotFound-Message")) {
                String message = Main.color(EnchantmentNotFoundMessage);
                sender.sendMessage(message);
            }
            return true;
        }

        int enchantmentLevel;
        int enchantMaxLvl = main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Enchantment-MaxLvl");
        try {
            enchantmentLevel = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            // Send InvalidLevel Message in chat when called.
            for (String InvalidLevelMessage : main.getSettingsConfig().getStringList("EnchantItem-GiveEnchant-Messages.EnchantItem-GiveEnchant-InvalidLevel-Message")) {
                String message = Main.color(InvalidLevelMessage)
                        .replace("{enchantMaxLvl}", String.valueOf(enchantMaxLvl));
                sender.sendMessage(message);
            }
            return true;
        }

        if (enchantmentLevel < 1 || enchantmentLevel > enchantMaxLvl) {
            // Send LevelRange Message in chat when called.
            for (String LevelRangeMessage : main.getSettingsConfig().getStringList("EnchantItem-GiveEnchant-Messages.EnchantItem-GiveEnchant-LevelRange-Message")) {
                String message = Main.color(LevelRangeMessage)
                        .replace("{enchantMaxLvl}", String.valueOf(enchantMaxLvl));
                sender.sendMessage(message);
            }
            return true;
        }

        int enchantmentAmount;
        try {
            enchantmentAmount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            // Send InvalidAmount Message in chat when called.
            for (String InvalidAmountMessage : main.getSettingsConfig().getStringList("EnchantItem-GiveEnchant-Messages.EnchantItem-GiveEnchant-InvalidAmount-Message")) {
                String message = Main.color(InvalidAmountMessage);
                sender.sendMessage(message);
            }
            return true;
        }

        if (enchantmentAmount < 1 || enchantmentAmount > 64) {
            // Send AmountRange Message in chat when called.
            for (String AmountRangeMessage : main.getSettingsConfig().getStringList("EnchantItem-GiveEnchant-Messages.EnchantItem-GiveEnchant-AmountRange-Message")) {
                String message = Main.color(AmountRangeMessage);
                sender.sendMessage(message);
            }
            return true;
        }

        // Create an instance of CreateCustomEnchant and call the method
        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(main);
        createCustomEnchant.CreateCustomEnchantmentItem(enchantmentName, enchantmentLevel, enchantmentAmount, player);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) { // Tab completing the second argument (enchant name)
            String input = args[1].toUpperCase(); // Current input typed by the player
            List<String> enchantments = main.getEnchantmentsConfig().getConfigurationSection("Enchantments").getKeys(false).stream()
                    .map(String::toUpperCase)
                    .filter(name -> name.toUpperCase().contains(input))
                    .collect(Collectors.toList());
            return enchantments.isEmpty() ? null : enchantments;
        } else if (args.length == 3) { // Tab completing the third argument (level)
            String enchantName = args[1].toUpperCase();
            if (main.getEnchantmentsConfig().contains("Enchantments." + enchantName)) {
                int maxLevel = main.getEnchantmentsConfig().getInt("Enchantments." + enchantName + ".Enchantment-MaxLvl");
                List<String> levels = new ArrayList<>();
                for (int i = 1; i <= maxLevel; i++) {
                    levels.add(String.valueOf(i));
                }
                return levels;
            }
        } else if (args.length == 4) { // Tab completing the fourth argument (amount)
            return Collections.singletonList("[1-64]");
        }
        return null; // Return null if no tab completions are found
    }
}
