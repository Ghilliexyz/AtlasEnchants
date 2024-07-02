package com.atlasplugins.atlasenchants.managers;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockRadiusFinder {

    private Main main;
    public BlockRadiusFinder (Main main) {
        this.main = main;
    }

    public List<Block> getBlocks(Block start, int radius) {
        List<Block> blocks = new ArrayList<>();

        // Calculate the bounds of the cube
        int minX = start.getX() - radius;
        int minY = start.getY() - radius;
        int minZ = start.getZ() - radius;
        int maxX = start.getX() + radius;
        int maxY = start.getY() + radius;
        int maxZ = start.getZ() + radius;

        World world = start.getWorld();

        // Iterate through all coordinates within the cube
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(world, x, y, z);
                    blocks.add(loc.getBlock());
                }
            }
        }

        return blocks;
    }
}
