package com.atlasplugins.atlasenchants.Commands;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.GlassPane;
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
        World world = player.getWorld();
        Material mat = Material.SPAWNER;

        for (Chunk chunk : world.getLoadedChunks())
        {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < world.getMaxHeight(); y++){
                    for (int z = 0; z < 16; z++)
                    {
                        Block block = chunk.getBlock(x, y, z);
                        if(block.getType() == mat)
                        {
                            try {
                            // Debugging information
                                Main.instance.glowingBlocks.setGlowing(block, player, ChatColor.GOLD);
                            // Ensure entity type is valid for glow effect
                            player.sendMessage("Glow effect applied to entity: " + block.getType());
                        } catch (Exception e) {
                            player.sendMessage("Failed to apply glow effect to entity: " + block.getType());
                        }
                        }
                    }
                }
            }
        }

        return true; // Command was handled
    }
}
