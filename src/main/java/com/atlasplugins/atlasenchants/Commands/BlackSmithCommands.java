package com.atlasplugins.atlasenchants.Commands;

import com.atlasplugins.atlasenchants.GUIs.BlackSmithGUI;
import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class BlackSmithCommands implements CommandExecutor, Listener {

    private Main main;

    public BlackSmithCommands(Main main) {
        this.main = main;
    }

    public int getEmptySlots(Player p) {
        Inventory inventory = p.getInventory();
        ItemStack[] cont = inventory.getContents();
        int i = 0;
        for (ItemStack item : cont) {
            if ((item != null) && (item.getType() != Material.AIR))
                i++;
        }
        return 36 - i;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length == 0) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (p.getInventory().firstEmpty() != -1) {
                    if (getEmptySlots(p) >= 2) {
                        BlackSmithGUI blackSmithGUI = new BlackSmithGUI(main);
                        blackSmithGUI.build(p);
                        blackSmithGUI.show(p);
                        p.updateInventory();
                    }
                } else {
                    p.sendMessage(Main.color(main.getConfig().getString("Messages.inventory-full-message-onopen")));
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
                }
            }
        } else if (sender.hasPermission("ae.admin")) {
            if (args[0].equalsIgnoreCase("help")) {
                String prefix = Main.color(main.getConfig().getString("Messages.tag-prefix"));

                sender.sendMessage("\n" + prefix);
                sender.sendMessage(Main.color("&c/ae &agive &e[player] &b[quantity] &f&l| &cGives you FearSight I Enchant"));
                sender.sendMessage(Main.color("&c/ae &areload &c| the config"));
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                main.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "Config Reloaded!");
                return true;
            }
            if (args.length == 3)
                if (Bukkit.getPlayer(args[1]) != null) {
                    Player target = Bukkit.getPlayer(args[1]);
                    String typeItem = main.getConfig().getString("Fearsight.customItem");
                    typeItem = typeItem.toUpperCase();
                    if (Material.getMaterial(typeItem) != null) {
                        ItemStack item = new ItemStack(Material.getMaterial(typeItem), Integer.parseInt(args[2]));
                        ItemMeta itemMeta = item.getItemMeta();
                        String lore = Main.color(main.getConfig().getString("Fearsight.lore-for-Fearsight"));
                        itemMeta.setDisplayName(Main.color("&cFearsight I"));
                        itemMeta.setLore(Arrays.asList(new String[]{lore}));
                        item.setItemMeta(itemMeta);
                        PlayerInventory inv = target.getInventory();
                        inv.addItem(new ItemStack[]{item});
                        return true;
                    }
                    Bukkit.broadcastMessage("item type.");
                } else {
                    Bukkit.broadcastMessage("player.");
                }
        }
        return false;
    }
}
