package com.atlasplugins.atlasenchants.Commands;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class GiveEnchantCommand implements CommandExecutor, Listener {

    private Main main;
    public GiveEnchantCommand (Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if(args.length < 4)
        {
            sender.sendMessage(Main.color("&cUsage: &7/giveenchant <player> <enchant> <level> <amount>"));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if(player == null)
        {
            sender.sendMessage(Main.color("&cPlayer Not Found"));
        }

        String enchantName = args[1].toUpperCase();
        Enchantment enchantment = Enchantment.getByName(enchantName);
        if(enchantment == null)
        {
            sender.sendMessage(Main.color("&cEnchantment Not Found"));
        }

        int enchantmentLevel;
        try {
            enchantmentLevel = Integer.parseInt(args[2]);
        } catch (NumberFormatException e)
        {
            sender.sendMessage(Main.color("&cInvalid Level"));
            return true;
        }

//        if(enchantmentLevel < 1 || enchantmentLevel > enchantment.getMaxLevel())
//        {
//            sender.sendMessage(Main.color("&cLevel must be between 1 and " + enchantment.getMaxLevel()));
//        }

        int enchantmentAmount;
        try {
            enchantmentAmount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e)
        {
            sender.sendMessage(Main.color("&cInvalid Amount"));
            return true;
        }

        if(enchantmentAmount < 1 || enchantmentAmount > 64)
        {
            sender.sendMessage(Main.color("&cYou can only spawn in 1-64 at a time."));
        }

        ItemStack enchant = new ItemStack(Material.valueOf(main.getConfig().getString("EnchantItems.CustomItem")));
        ItemMeta enchantMeta = enchant.getItemMeta();

        enchantMeta.setDisplayName(Main.color(main.getConfig().getString("FEARSIGHT.Fearsight-Title") + " &eLvl: " + enchantmentLevel));

        ArrayList<String> enchantmentLore = new ArrayList();
        for (String EnchantmentLore : main.getConfig().getStringList("FEARSIGHT.Fearsight-Lore"))
        {
            enchantmentLore.add(Main.color(EnchantmentLore)
                    .replace("{lvl}", String.valueOf(enchantmentLevel))
                    .replace("{range}", String.valueOf(main.getConfig().getString("FEARSIGHT.radius-of-glowing-"+enchantmentLevel))));
        }

        enchantMeta.setLore(enchantmentLore);
        enchant.setItemMeta(enchantMeta);

        for (int i = 0; i < enchantmentAmount; i++)
        {
            player.getInventory().addItem(enchant);
        }

        return false;
    }
}
