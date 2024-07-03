package com.atlasplugins.atlasenchants;

import com.atlasplugins.atlasenchants.commands.CommandRouter;
import com.atlasplugins.atlasenchants.enchants.armor.BlessingofKnowledge;
import com.atlasplugins.atlasenchants.enchants.armor.Fearsight;
import com.atlasplugins.atlasenchants.enchants.armor.Rush;
import com.atlasplugins.atlasenchants.enchants.defense.EnergyAbsorption;
import com.atlasplugins.atlasenchants.enchants.tools.*;
import com.atlasplugins.atlasenchants.enchants.weapons.*;
import com.atlasplugins.atlasenchants.listeners.enchantevents.ApplyCustomEnchant;
import com.atlasplugins.atlasenchants.listeners.armorevents.ArmorEquipListener;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateCustomEnchant;
import com.atlasplugins.atlasenchants.listeners.enchantevents.LootTableEvent;
import com.atlasplugins.atlasenchants.managers.BlockRadiusFinder;
import com.atlasplugins.atlasenchants.managers.LogsPlacedManager;
import com.atlasplugins.atlasenchants.managers.OresPlacedManager;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Main extends JavaPlugin implements Listener {

    public static Main instance;

    // Change chat colors
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    // Glowing Stuff
    public GlowingEntities glowingEntities;
    public GlowingBlocks glowingBlocks;

    // Enchantment Stuff
    public static NamespacedKey customEnchantKeys;
    // Spawner Stuff
    public static NamespacedKey spawnerKeys;
    // Logs Placed Stuff
    private LogsPlacedManager logsPlacedManager;
    // Ores Placed Stuff
    private OresPlacedManager oresPlacedManager;
    // Block Radius Finder Stuff
    public BlockRadiusFinder blockRadiusFinder;

    // Config Stuff
    private FileConfiguration enchantmentsConfig;
    private File enchantmentsConfigFile;
    private FileConfiguration settingsConfig;
    private File settingsConfigFile;

    // Command Router Stuff
    private CommandRouter commandRouter;

    private boolean isPlaceholderAPIPresent;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        // check if placeholderAPI is present on the server.
        isPlaceholderAPIPresent = checkForPlaceholderAPI();
        if (isPlaceholderAPIPresent) {
            getLogger().info("PlaceholderAPI found, placeholders will be used.");
        } else {
            getLogger().info("PlaceholderAPI not found, placeholders will not be used.");
        }

        // Set up custom enchants
        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(this);

        // Load custom configs
        loadEnchantmentsConfig();
        loadSettingsConfig();

        // Set up Glowing
        glowingEntities = new GlowingEntities(this);
        glowingBlocks = new GlowingBlocks(this);

        //Custom Enchant Data
        customEnchantKeys = new NamespacedKey(this, "Custom_Enchants");
        //Spawner Data
        spawnerKeys = new NamespacedKey(this, "Spawners");
        // Initialize PlayerPlacedBlocksManager
        logsPlacedManager = new LogsPlacedManager(this);
        // Initialize PlayerPlacedBlocksManager
        oresPlacedManager = new OresPlacedManager(this);
        // Initialize BlockUtils instance
        blockRadiusFinder = new BlockRadiusFinder(this);

        // Plugin Started Message
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e1.3.1"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie & Helix"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &aEnabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));

        //All Enchants
        this.getServer().getPluginManager().registerEvents(new Fearsight(this),this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Leech(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Hunter(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Rush(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Propel(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new FreezingShot(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new BlessingofKnowledge(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new SafeMiner(this), this); // Added By Helix
        this.getServer().getPluginManager().registerEvents(new PoisonAspect(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Stunning(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new IceAspect(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Extractor(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new EnergyAbsorption(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Regrowth(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new TreeHugger(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new MinersTouch(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new VeinSeeker(this), this); // Added By Ghillie
        //All Events
        this.getServer().getPluginManager().registerEvents(new ApplyCustomEnchant(this), this);
        this.getServer().getPluginManager().registerEvents(new ArmorEquipListener(), this);
        this.getServer().getPluginManager().registerEvents(new CreateCustomEnchant(this), this);
        this.getServer().getPluginManager().registerEvents(new LootTableEvent(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        // Register commands
        this.commandRouter = new CommandRouter(this);
        getCommand("atlasenchants").setExecutor(commandRouter);
        getCommand("atlasenchants").setTabCompleter(commandRouter);

        // BStats Info
        int pluginId = 22376; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        glowingEntities.disable();
        glowingBlocks.disable();

        // Save data to file on plugin disable
        logsPlacedManager.saveDataToFile();
        // Save data to file on plugin disable
        oresPlacedManager.saveDataToFile();

        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e1.3.1"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie & Helix"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &4Disabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
    }

    public LogsPlacedManager getLogsPlacedManager() {
        return logsPlacedManager;
    }

    public OresPlacedManager getOresPlacedManager() {
        return oresPlacedManager;
    }

    public String setPlaceholders(Player p, String text)
    {
        if(checkForPlaceholderAPI())
        {
            return PlaceholderAPI.setPlaceholders(p, text);
        }else{
            return text;
        }
    }

    private boolean checkForPlaceholderAPI() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        return plugin != null && plugin.isEnabled();
    }

    public FileConfiguration getEnchantmentsConfig() {
        return enchantmentsConfig;
    }

    public void saveEnchantmentsConfig() {
        try {
            enchantmentsConfig.save(enchantmentsConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadEnchantmentsConfig() {
        enchantmentsConfigFile = new File(getDataFolder(), "enchantments.yml");
        if (!enchantmentsConfigFile.exists()) {
            saveResource("enchantments.yml", false);
        }
        enchantmentsConfig = YamlConfiguration.loadConfiguration(enchantmentsConfigFile);
    }

    public FileConfiguration getSettingsConfig() {
        return settingsConfig;
    }

    public void saveSettingsConfig() {
        try {
            settingsConfig.save(settingsConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSettingsConfig() {
        settingsConfigFile = new File(getDataFolder(), "settings.yml");
        if (!settingsConfigFile.exists()) {
            saveResource("settings.yml", false);
        }
        settingsConfig = YamlConfiguration.loadConfiguration(settingsConfigFile);
    }
}
