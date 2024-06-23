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
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.color("&c&m&l------------&f&l [&x&F&F&3&C&3&C&lA&x&F&F&4&C&3&E&lt&x&F&E&5&C&4&0&ll&x&F&E&6&C&4&2&la&x&F&E&7&C&4&4&ls &x&F&D&8&C&4&6&lE&x&F&D&9&C&4&8&ln&x&F&D&A&B&4&A&lc&x&F&C&B&B&4&C&lh&x&F&C&C&B&4&E&la&x&F&C&D&B&5&0&ln&x&F&B&E&B&5&2&lt&x&F&B&F&B&5&4&ls&f&l] &c&m&l-------------"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c● &7This command can only be used by players."));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c&m&l-----------------------------------------"));
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(Main.color("&c&m&l------------&f&l [&x&F&F&3&C&3&C&lA&x&F&F&4&C&3&E&lt&x&F&E&5&C&4&0&ll&x&F&E&6&C&4&2&la&x&F&E&7&C&4&4&ls &x&F&D&8&C&4&6&lE&x&F&D&9&C&4&8&ln&x&F&D&A&B&4&A&lc&x&F&C&B&B&4&C&lh&x&F&C&C&B&4&E&la&x&F&C&D&B&5&0&ln&x&F&B&E&B&5&2&lt&x&F&B&F&B&5&4&ls&f&l] &c&m&l-------------"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c● &7Usage: &c/giveenchant &e<player> <enchant> <level> <amount>"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c&m&l-----------------------------------------"));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(Main.color("&c&m&l------------&f&l [&x&F&F&3&C&3&C&lA&x&F&F&4&C&3&E&lt&x&F&E&5&C&4&0&ll&x&F&E&6&C&4&2&la&x&F&E&7&C&4&4&ls &x&F&D&8&C&4&6&lE&x&F&D&9&C&4&8&ln&x&F&D&A&B&4&A&lc&x&F&C&B&B&4&C&lh&x&F&C&C&B&4&E&la&x&F&C&D&B&5&0&ln&x&F&B&E&B&5&2&lt&x&F&B&F&B&5&4&ls&f&l] &c&m&l-------------"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c● &7Player Not Found"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c&m&l-----------------------------------------"));

            return true;
        }

        String enchantmentName = args[1].toUpperCase();
        if (!main.getConfig().contains("Enchantments." + enchantmentName)) {
            sender.sendMessage(Main.color("&c&m&l------------&f&l [&x&F&F&3&C&3&C&lA&x&F&F&4&C&3&E&lt&x&F&E&5&C&4&0&ll&x&F&E&6&C&4&2&la&x&F&E&7&C&4&4&ls &x&F&D&8&C&4&6&lE&x&F&D&9&C&4&8&ln&x&F&D&A&B&4&A&lc&x&F&C&B&B&4&C&lh&x&F&C&C&B&4&E&la&x&F&C&D&B&5&0&ln&x&F&B&E&B&5&2&lt&x&F&B&F&B&5&4&ls&f&l] &c&m&l-------------"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c● &7Enchantment Not Found"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c&m&l-----------------------------------------"));
            return true;
        }

        int enchantmentLevel;
        try {
            enchantmentLevel = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Main.color("&c&m&l------------&f&l [&x&F&F&3&C&3&C&lA&x&F&F&4&C&3&E&lt&x&F&E&5&C&4&0&ll&x&F&E&6&C&4&2&la&x&F&E&7&C&4&4&ls &x&F&D&8&C&4&6&lE&x&F&D&9&C&4&8&ln&x&F&D&A&B&4&A&lc&x&F&C&B&B&4&C&lh&x&F&C&C&B&4&E&la&x&F&C&D&B&5&0&ln&x&F&B&E&B&5&2&lt&x&F&B&F&B&5&4&ls&f&l] &c&m&l-------------"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c● &7Invalid Level"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c&m&l-----------------------------------------"));
            return true;
        }

        int enchantMaxLvl = main.getConfig().getInt("Enchantments." + enchantmentName + ".Enchantment-MaxLvl");
        if (enchantmentLevel < 1 || enchantmentLevel > enchantMaxLvl) {
            sender.sendMessage(Main.color("&c&m&l------------&f&l [&x&F&F&3&C&3&C&lA&x&F&F&4&C&3&E&lt&x&F&E&5&C&4&0&ll&x&F&E&6&C&4&2&la&x&F&E&7&C&4&4&ls &x&F&D&8&C&4&6&lE&x&F&D&9&C&4&8&ln&x&F&D&A&B&4&A&lc&x&F&C&B&B&4&C&lh&x&F&C&C&B&4&E&la&x&F&C&D&B&5&0&ln&x&F&B&E&B&5&2&lt&x&F&B&F&B&5&4&ls&f&l] &c&m&l-------------"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c● &7Level must be between &e1 &7and &e" + enchantMaxLvl));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c&m&l-----------------------------------------"));
            return true;
        }

        int enchantmentAmount;
        try {
            enchantmentAmount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Main.color("&c&m&l------------&f&l [&x&F&F&3&C&3&C&lA&x&F&F&4&C&3&E&lt&x&F&E&5&C&4&0&ll&x&F&E&6&C&4&2&la&x&F&E&7&C&4&4&ls &x&F&D&8&C&4&6&lE&x&F&D&9&C&4&8&ln&x&F&D&A&B&4&A&lc&x&F&C&B&B&4&C&lh&x&F&C&C&B&4&E&la&x&F&C&D&B&5&0&ln&x&F&B&E&B&5&2&lt&x&F&B&F&B&5&4&ls&f&l] &c&m&l-------------"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c● &7Invalid Amount"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c&m&l-----------------------------------------"));
            return true;
        }

        if (enchantmentAmount < 1 || enchantmentAmount > 64) {
            sender.sendMessage(Main.color("&c&m&l------------&f&l [&x&F&F&3&C&3&C&lA&x&F&F&4&C&3&E&lt&x&F&E&5&C&4&0&ll&x&F&E&6&C&4&2&la&x&F&E&7&C&4&4&ls &x&F&D&8&C&4&6&lE&x&F&D&9&C&4&8&ln&x&F&D&A&B&4&A&lc&x&F&C&B&B&4&C&lh&x&F&C&C&B&4&E&la&x&F&C&D&B&5&0&ln&x&F&B&E&B&5&2&lt&x&F&B&F&B&5&4&ls&f&l] &c&m&l-------------"));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c● &7You can only spawn in &e1-64 &7at a time."));
            sender.sendMessage(Main.color(""));
            sender.sendMessage(Main.color("&c&m&l-----------------------------------------"));
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
            List<String> enchantments = main.getConfig().getConfigurationSection("Enchantments").getKeys(false).stream()
                    .map(String::toUpperCase)
                    .filter(name -> name.toUpperCase().contains(input))
                    .collect(Collectors.toList());
            return enchantments.isEmpty() ? null : enchantments;
        } else if (args.length == 3) { // Tab completing the third argument (level)
            String enchantName = args[1].toUpperCase();
            if (main.getConfig().contains("Enchantments." + enchantName)) {
                int maxLevel = main.getConfig().getInt("Enchantments." + enchantName + ".Enchantment-MaxLvl");
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
