package com.atlasplugins.atlasenchants.Commands;

import com.atlasplugins.atlasenchants.GUIs.shop;
import com.atlasplugins.atlasenchants.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private Main main;
    public ShopCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        Player player = (Player) commandSender;

        shop shop = new shop(main);
        shop.build(player);
        shop.show(player);
        player.updateInventory();

        return false;
    }

}
