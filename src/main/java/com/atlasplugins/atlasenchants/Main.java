package com.atlasplugins.atlasenchants;

import com.atlasplugins.atlasenchants.Commands.BlackSmithCommand;
import com.atlasplugins.atlasenchants.Commands.GiveEnchantCommand;
import com.atlasplugins.atlasenchants.Commands.ShopCommand;
import com.atlasplugins.atlasenchants.Enchants.Rare.Fearsight;
import com.atlasplugins.atlasenchants.Listeners.onInventoryClick;
import com.atlasplugins.atlasenchants.Listeners.onPlayerJoin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public final class Main extends JavaPlugin {

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

        //All Events
        this.getServer().getPluginManager().registerEvents(new onPlayerJoin(this), this);
        this.getServer().getPluginManager().registerEvents(new Fearsight(this), this);
        this.getServer().getPluginManager().registerEvents(new onInventoryClick(this), this);

        //ALl Commands
        this.getCommand("giveenchant").setExecutor(new GiveEnchantCommand(this));
        this.getCommand("blacksmith").setExecutor(new BlackSmithCommand(this));
        this.getCommand("shop").setExecutor(new ShopCommand(this));

//        this.getServer().getPluginManager().registerEvents(new BlackSmith(this),this);
//        this.getServer().getPluginManager().registerEvents(new BlackSmithCommands(this),this);
//        this.getServer().getPluginManager().registerEvents(new Shop(this),this);
//        this.getServer().getPluginManager().registerEvents(new GodlyEnchants(this),this);
//        this.getServer().getPluginManager().registerEvents(new InvinClickEvent(this),this);
//        this.getServer().getPluginManager().registerEvents(new FearsightShop(this),this);


//        Bukkit.getServer().getPluginManager().registerEvents(this, this);
//        //All Commands
//        this.getCommand("blacksmith").setExecutor(new BlackSmithCommands(this));
//        this.getCommand("shop").setExecutor(new ShopCommand(this));
//        //Testing Command
//        this.getCommand("test").setExecutor(new test(this));
//        this.getServer().getPluginManager().registerEvents(new test(this),this);

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
