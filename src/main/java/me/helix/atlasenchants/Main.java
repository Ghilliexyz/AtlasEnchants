package me.helix.atlasenchants;

import me.helix.atlasenchants.Commands.BlackSmithCommands;
import me.helix.atlasenchants.Commands.ShopCommand;
import me.helix.atlasenchants.Commands.test;
import me.helix.atlasenchants.Enchants.Fearsight;
import me.helix.atlasenchants.GUIs.BlackSmithGUI;
import me.helix.atlasenchants.GUIs.FearSight.FearsightShop;
import me.helix.atlasenchants.GUIs.ShopGUI;
import me.helix.atlasenchants.Listeners.InventoryClick;
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

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }

        Bukkit.getConsoleSender().sendMessage(color("&7============================================="));
        Bukkit.getConsoleSender().sendMessage(color("&l&eAtlas Custom Enchants &7: &a2&7.&a0&e+"));
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(color("&eMade by &7: &eChelsea1124&7/&eHELIX & Ghillie"));
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(color("&eVersion &7: &a2&7.&a0&e+"));
        Bukkit.getConsoleSender().sendMessage(color("&7============================================="));

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
    }
}
