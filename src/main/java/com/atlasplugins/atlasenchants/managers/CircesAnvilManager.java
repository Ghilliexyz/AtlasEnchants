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

public class CircesAnvilManager {

    private final Main main;
    private final File dataFile;
    private final Map<String, Boolean> circesAnvilLocations;
    private final ObjectMapper objectMapper;

    public CircesAnvilManager(Main main) {
        this.main = main;
        this.dataFile = new File(main.getDataFolder(), "circes_anvil_locations.json");
        this.circesAnvilLocations = new HashMap<>();
        this.objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

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

        if (dataFile.length() == 0) {
            return;
        }

        try {
            circesAnvilLocations.putAll(objectMapper.readValue(dataFile, Map.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDataToFile() {
        try {
            objectMapper.writeValue(dataFile, circesAnvilLocations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCircesAnvil(Block block) {
        Location location = block.getLocation();
        String key = getKey(location);
        return circesAnvilLocations.containsKey(key) && circesAnvilLocations.get(key);
    }

    public void markCircesAnvil(Block block) {
        Location location = block.getLocation();
        String key = getKey(location);
        circesAnvilLocations.put(key, true);
        saveDataToFile();
    }

    public void removeCircesAnvil(Block block) {
        Location location = block.getLocation();
        String key = getKey(location);
        if (circesAnvilLocations.containsKey(key)) {
            circesAnvilLocations.remove(key);
            saveDataToFile();
        }
    }

    private String getKey(Location location) {
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }
}
