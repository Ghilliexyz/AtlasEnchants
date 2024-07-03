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

    public List<Block> getBlocks(Block start, int radiusX, int radiusY, int radiusZ) {
        List<Block> blocks = new ArrayList<>();

        // Calculate the bounds of the cube
        int minX = start.getX() - radiusX;
        int minY = start.getY() - radiusY;
        int minZ = start.getZ() - radiusZ;
        int maxX = start.getX() + radiusX;
        int maxY = start.getY() + radiusY;
        int maxZ = start.getZ() + radiusZ;

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
