package com.atlasplugins.atlasenchants.Commands;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import ru.xezard.glow.data.glow.Glow;

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

        Glow glowRed = null;
        try {
            glowRed = Glow.builder()
                    .color(ChatColor.DARK_RED)
                    .name("redglow")
                    .build();
            player.sendMessage("Glow effect created successfully.");

            glowRed.display(player);
            player.sendMessage(player.getName() + " Added successfully to " + glowRed.getName());
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("Failed to create glow effect.");
            return true;
        }

        for (Entity entity : player.getNearbyEntities(50,50,50))
        {
            if (entity instanceof Slime)
            {
                try {
                    // Debugging information
                    player.sendMessage("Processing entity: " + entity.getName() + " (ID: " + entity.getEntityId() + ")");
                    System.out.println("Processing entity: " + entity.getName() + " (ID: " + entity.getEntityId() + ")");

                    // Ensure entity type is valid for glow effect
                        glowRed.addHolders(entity);
                        player.sendMessage("Glow effect applied to entity: " + entity.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage("Failed to apply glow effect to entity: " + entity.getName());
                }
            }
        }

        return true; // Command was handled
    }
}
