package com.atlasplugins.atlasenchants;

import com.atlasplugins.atlasenchants.Commands.GiveEnchantCommand;
import com.atlasplugins.atlasenchants.Commands.TestCommand;
import com.atlasplugins.atlasenchants.Enchants.Fearsight;
import com.atlasplugins.atlasenchants.Listeners.ApplyCustomEnchant;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Main extends JavaPlugin implements Listener {

    public static Main instance;

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public GlowingEntities glowingEntities;
    public GlowingBlocks glowingBlocks;

    public static NamespacedKey customEnchantKeys;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        glowingEntities = new GlowingEntities(this);
        glowingBlocks = new GlowingBlocks(this);

        //Custom Enchant Data
        customEnchantKeys = new NamespacedKey(this, "Custom_Enchants");

        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e1.1"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie & Helix"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &aEnabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));

        //All Events
        this.getServer().getPluginManager().registerEvents(new Fearsight(this),this);
        this.getServer().getPluginManager().registerEvents(new ApplyCustomEnchant(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        //All Commands
        this.getCommand("giveenchant").setExecutor(new GiveEnchantCommand(this));
        this.getCommand("test").setExecutor(new TestCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        glowingEntities.disable();
        glowingBlocks.disable();
    }
}
