package com.atlasplugins.atlasenchants.Commands;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class GiveEnchantCommand implements CommandExecutor {

    private Main main;
    public GiveEnchantCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        Player p = (Player) commandSender;

        ItemStack vetro = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta vetroMeta = vetro.getItemMeta();
        vetroMeta.setDisplayName(Main.color("&cFearsight I"));
        vetro.setItemMeta(vetroMeta);

        PlayerInventory inv = p.getInventory();
        inv.addItem(new ItemStack[] { vetro });

        return false;
    }
}
