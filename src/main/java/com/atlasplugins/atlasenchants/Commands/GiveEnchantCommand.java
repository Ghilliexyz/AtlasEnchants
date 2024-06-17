package com.atlasplugins.atlasenchants.Commands;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class GiveEnchantCommand implements CommandExecutor {

    private Main main;

    public GiveEnchantCommand(Main main) {
        this.main = main;
    }

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

        String enchantName = args[1].toUpperCase();
        if (!main.getConfig().contains("Enchantments." + enchantName)) {
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

        int enchantMaxLvl = main.getConfig().getInt("Enchantments." + enchantName + ".Enchantment-MaxLvl");
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

        ItemStack enchant = new ItemStack(Material.valueOf(main.getConfig().getString("EnchantItems.CustomItem")));
        ItemMeta enchantMeta = enchant.getItemMeta();

        enchantMeta.setDisplayName(Main.color(main.getConfig().getString("Enchantments." + enchantName + ".Enchantment-Title"))
                    .replace("{lvl}", String.valueOf(enchantmentLevel))
                    .replace("{range}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantName + ".Radius-of-glowing-" + enchantmentLevel)))
                    .replace("{time}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantName + ".Time-underwater-" + enchantmentLevel)))
                    .replace("{damage}", String.valueOf(main.getConfig().getDouble("Enchantments." + enchantName + ".Hunter-Damage-Amount-" + enchantmentLevel)))
                    .replace("{percent}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantName + ".Leech-Healing-Amount-Percent-" + enchantmentLevel))));


        ArrayList<String> enchantmentLore = new ArrayList<>();
        List<String> loreList = main.getConfig().getStringList("Enchantments." + enchantName + ".Enchantment-Lore");
        for (String lore : loreList) {
            enchantmentLore.add(Main.color(lore)
                    .replace("{lvl}", String.valueOf(enchantmentLevel))
                    .replace("{range}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantName + ".Radius-of-glowing-" + enchantmentLevel)))
                    .replace("{time}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantName + ".Time-underwater-" + enchantmentLevel)))
                    .replace("{damage}", String.valueOf(main.getConfig().getDouble("Enchantments." + enchantName + ".Hunter-Damage-Amount-" + enchantmentLevel)))
                    .replace("{percent}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantName + ".Leech-Healing-Amount-Percent-" + enchantmentLevel))));
        }

        PersistentDataContainer pdc = enchantMeta.getPersistentDataContainer();
        pdc.set(Main.customEnchantKeys, PersistentDataType.STRING, enchantName + ":" + enchantmentLevel);

        enchantMeta.setLore(enchantmentLore);
        enchant.setItemMeta(enchantMeta);

        for (int i = 0; i < enchantmentAmount; i++) {
            player.getInventory().addItem(enchant);
        }

        return true;
    }
}