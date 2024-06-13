package me.helix.atlasenchants.Commands;

import me.helix.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class test implements CommandExecutor, Listener {

    private Main main;
    public test (Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        Player player = (Player) commandSender;

        ItemStack vetro = new ItemStack(Material.HEART_OF_THE_SEA);
        ItemMeta vetroMeta = vetro.getItemMeta();
        vetroMeta.setDisplayName(Main.color("&cFearsight I"));
        vetro.setItemMeta(vetroMeta);

        PlayerInventory inv = player.getInventory();
        inv.addItem(new ItemStack[] { vetro });

        return false;
    }

}
