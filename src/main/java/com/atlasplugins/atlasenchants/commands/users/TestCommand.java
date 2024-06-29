package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

public class TestCommand implements CommandExecutor, Listener {


    private Main main;
    public TestCommand (Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//        if (!(sender instanceof Player)) {
//            sender.sendMessage("This command can only be used by players.");
//            return true;
//        }
//
//        Player player = (Player) sender;
//        World world = player.getWorld();
//        Material mat = Material.SHIELD;
//
//        for (Chunk chunk : world.getLoadedChunks())
//        {
//            for (int x = 0; x < 16; x++) {
//                for (int y = 0; y < world.getMaxHeight(); y++){
//                    for (int z = 0; z < 16; z++)
//                    {
//                        Block block = chunk.getBlock(x, y, z);
//                        if(block.getType() == mat)
//                        {
//                            try {
//                            // Debugging information
//                                Main.instance.glowingBlocks.setGlowing(block, player, ChatColor.GOLD);
//                            // Ensure entity type is valid for glow effect
//                            player.sendMessage("Glow effect applied to entity: " + block.getType());
//                        } catch (Exception e) {
//                            player.sendMessage("Failed to apply glow effect to entity: " + block.getType());
//                        }
//                        }
//                    }
//                }
//            }
//        }

        return false;
    }

}
