package com.atlasplugins.atlasenchants;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Main extends JavaPlugin {
    public static Main instance;
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    @Override
    public void onEnable() {

        instance = this;
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e1.0"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie & Helix"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &aEnabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));

    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e1.0"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie & Helix"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &4Enabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
    }
}
