package com.atlasplugins.atlasenchants;

import com.atlasplugins.atlasenchants.Commands.BlackSmithCommands;
import com.atlasplugins.atlasenchants.Commands.ShopCommand;
import com.atlasplugins.atlasenchants.Commands.test;
import com.atlasplugins.atlasenchants.Enchants.Fearsight;
import com.atlasplugins.atlasenchants.GUIs.FearSight.FearsightShop;
import com.atlasplugins.atlasenchants.Listeners.InventoryClick;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public final class Main extends JavaPlugin implements Listener {

    public HashMap<Player, Inventory> BlackSmithInventory = new HashMap<Player, Inventory>();
    public HashMap<Player, Inventory> ShopInventory = new HashMap<Player, Inventory>();
    public HashMap<Player, Inventory> FearSightInventory = new HashMap<>();
    public HashMap<Player, Inventory> GodlyEnchantInventory = new HashMap<>();
    public HashMap<Player, Boolean> hasHelmet = new HashMap<>();
    public HashMap<Player, List<Entity>> playerEntities = new HashMap<>();
    public HashMap<Player, BukkitTask> ColorTask = new HashMap<>();

    public static Main instance;

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public GlowingEntities glowingEntities;
    public GlowingBlocks glowingBlocks;

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


        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e1.0"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie & Helix"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &aEnabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));

        //All Events
//        this.getServer().getPluginManager().registerEvents((Listener) new BlackSmithGUI(this),this);
        this.getServer().getPluginManager().registerEvents(new BlackSmithCommands(this),this);
        this.getServer().getPluginManager().registerEvents(new Fearsight(this),this);

//        this.getServer().getPluginManager().registerEvents((Listener) new ShopGUI(this),this);
        this.getServer().getPluginManager().registerEvents(new InventoryClick(this),this);
        this.getServer().getPluginManager().registerEvents(new FearsightShop(this),this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        //All Commands
        this.getCommand("blacksmith").setExecutor(new BlackSmithCommands(this));
        this.getCommand("shop").setExecutor(new ShopCommand(this));
        //Testing Command
        this.getCommand("test").setExecutor(new test(this));
        this.getServer().getPluginManager().registerEvents(new test(this),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        glowingEntities.disable();
        glowingBlocks.disable();
    }
}
