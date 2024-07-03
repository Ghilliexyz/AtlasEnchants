package com.atlasplugins.atlasenchants.managers;

import com.atlasplugins.atlasenchants.Main;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OresPlacedManager {

    private final Main main;
    private final File dataFile;
    private final Map<String, Boolean> playerPlacedLogs; // Map to store player-placed log block data
    private final ObjectMapper objectMapper;

    public OresPlacedManager(Main main) {
        this.main = main;
        this.dataFile = new File(main.getDataFolder(), "player_placed_logs.json");
        this.playerPlacedLogs = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Enable pretty printing

        // Load data from file on plugin enable
        loadDataFromFile();
    }

    private void loadDataFromFile() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            // Read JSON content from file and parse into playerPlacedLogs map
            playerPlacedLogs.putAll(objectMapper.readValue(dataFile, Map.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDataToFile() {
        try {
            // Serialize playerPlacedLogs map to JSON and write to file
            objectMapper.writeValue(dataFile, playerPlacedLogs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isPlayerPlacedLog(Block block) {
        Location location = block.getLocation();
        String key = getKey(location);
        return playerPlacedLogs.containsKey(key) && playerPlacedLogs.get(key);
    }

    public void markPlayerPlacedLog(Block block) {
        Location location = block.getLocation();
        String key = getKey(location);
        playerPlacedLogs.put(key, true);
        saveDataToFile();
    }

    public void removePlayerPlacedLog(Block block) {
        Location location = block.getLocation();
        String key = getKey(location);
        if (playerPlacedLogs.containsKey(key)) {
            playerPlacedLogs.remove(key);
        }
    }

    private String getKey(Location location) {
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }
}
