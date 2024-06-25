package com.atlasplugins.atlasenchants;

import com.atlasplugins.atlasenchants.Commands.GiveEnchantCommand;
import com.atlasplugins.atlasenchants.Commands.ReloadConfigsCommand;
import com.atlasplugins.atlasenchants.Commands.TestCommand;
import com.atlasplugins.atlasenchants.Enchants.Armor.BlessingofKnowledge;
import com.atlasplugins.atlasenchants.Enchants.Armor.Fearsight;
//import com.atlasplugins.atlasenchants.Enchants.Armor.Growth;
import com.atlasplugins.atlasenchants.Enchants.Armor.Rush;
import com.atlasplugins.atlasenchants.Enchants.Tools.SafeMiner;
import com.atlasplugins.atlasenchants.Enchants.Weapons.*;
import com.atlasplugins.atlasenchants.Listeners.ApplyCustomEnchant;
import com.atlasplugins.atlasenchants.Listeners.ArmorEquipListener;
import com.atlasplugins.atlasenchants.Listeners.CreateCustomEnchant;
import com.atlasplugins.atlasenchants.Listeners.LootTableEvent;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Main extends JavaPlugin implements Listener {

    public static Main instance;

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public GlowingEntities glowingEntities;
    public GlowingBlocks glowingBlocks;

    public static NamespacedKey customEnchantKeys;

    private FileConfiguration enchantmentsConfig;
    private File enchantmentsConfigFile;
    private FileConfiguration settingsConfig;
    private File settingsConfigFile;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(this);

        // Load custom configs
        loadEnchantmentsConfig();
        loadSettingsConfig();

        glowingEntities = new GlowingEntities(this);
        glowingBlocks = new GlowingBlocks(this);

        //Custom Enchant Data
        customEnchantKeys = new NamespacedKey(this, "Custom_Enchants");

        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e1.3.0"));
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
//        this.getServer().getPluginManager().registerEvents(new Growth(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new SafeMiner(this), this); // Added By Helix
        this.getServer().getPluginManager().registerEvents(new PoisonAspect(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Stunning(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new IceAspect(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Extractor(this), this); // Added By Ghillie
        //All Events
        this.getServer().getPluginManager().registerEvents(new ApplyCustomEnchant(this), this);
        this.getServer().getPluginManager().registerEvents(new ArmorEquipListener(), this);
        this.getServer().getPluginManager().registerEvents(new CreateCustomEnchant(this), this);
        this.getServer().getPluginManager().registerEvents(new LootTableEvent(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        //All Commands
        this.getCommand("giveenchant").setExecutor(new GiveEnchantCommand(this));
        this.getCommand("giveenchant").setTabCompleter(new GiveEnchantCommand(this));
        this.getCommand("reloadconfig").setExecutor(new ReloadConfigsCommand(this));
        this.getCommand("test").setExecutor(new TestCommand(this));

        // BStats Info
//        int pluginId = 22376; // <-- Replace with the id of your plugin!
//        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        glowingEntities.disable();
        glowingBlocks.disable();

        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e1.3.0"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie & Helix"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &4Enabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
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
