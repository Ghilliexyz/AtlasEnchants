package com.atlasplugins.atlasenchants.Commands;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.Listener;

public class test implements CommandExecutor, Listener {

    private Main main;
    public test (Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        for (Entity entity : player.getNearbyEntities(15,15,15))
        {
            if (entity instanceof Slime)
            {
                try {
                    // Debugging information
                    player.sendMessage("Processing entity: " + entity.getName() + " (ID: " + entity.getEntityId() + ")");

                    Main.instance.glowingEntities.setGlowing(entity, player, ChatColor.RED);
                    // Ensure entity type is valid for glow effect
                        player.sendMessage("Glow effect applied to entity: " + entity.getName());
                } catch (Exception e) {
                    player.sendMessage("Failed to apply glow effect to entity: " + entity.getName());
                }
            }
        }

        return true; // Command was handled
    }
}
