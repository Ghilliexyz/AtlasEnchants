package com.atlasplugins.atlasenchants;

import com.atlasplugins.atlasenchants.commands.CommandRouter;
import com.atlasplugins.atlasenchants.enchants.armor.*;
import com.atlasplugins.atlasenchants.enchants.defense.EnergyAbsorption;
import com.atlasplugins.atlasenchants.enchants.defense.FinalGuard;
import com.atlasplugins.atlasenchants.enchants.tools.*;
import com.atlasplugins.atlasenchants.enchants.weapons.*;
import com.atlasplugins.atlasenchants.guis.*;
import com.atlasplugins.atlasenchants.listeners.OraclesOfEnchantmentEvent;
import com.atlasplugins.atlasenchants.listeners.enchantevents.*;
import com.atlasplugins.atlasenchants.listeners.armorevents.ArmorEquipListener;
import com.atlasplugins.atlasenchants.managers.BlockRadiusFinder;
import com.atlasplugins.atlasenchants.managers.ExperienceManager;
import com.atlasplugins.atlasenchants.managers.LogsPlacedManager;
import com.atlasplugins.atlasenchants.managers.OresPlacedManager;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Main extends JavaPlugin implements Listener {

    public static Main instance;

    private ExperienceManager experienceManager;

    // Change chat colors
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    // Glowing Stuff
    public GlowingEntities glowingEntities;

    // Enchantment Stuff
    public static NamespacedKey customEnchantKeys;
    public static NamespacedKey customShardKeys;
    public static NamespacedKey customOracleKeys;
    // Spawner Stuff
    public static NamespacedKey spawnerKeys;
    // Logs Placed Stuff
    private LogsPlacedManager logsPlacedManager;
    // Ores Placed Stuff
    private OresPlacedManager oresPlacedManager;
    // Block Radius Finder Stuff
    public BlockRadiusFinder blockRadiusFinder;

    // PlaceholderAPI
    private boolean isPlaceholderAPIPresent;
    // WorldGuardAPI
    private boolean isWorldGuardAPIPresent;
    private WorldGuardPlugin worldGuardPlugin;

    // Config Stuff
    private FileConfiguration enchantmentsConfig;
    private File enchantmentsConfigFile;
    private FileConfiguration settingsConfig;
    private File settingsConfigFile;
    private FileConfiguration menusConfig;
    private File menusConfigFile;

    // Command Router Stuff
    private CommandRouter commandRouter;

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

        // check if WorldGuardAPI is present on the server.
        isWorldGuardAPIPresent = checkForWorldGuardAPI();
        if (isWorldGuardAPIPresent) {
            getLogger().info("WorldGuardAPI found, worldguard will be used.");
        } else {
            getLogger().info("WorldGuardAPI not found, worldguard will not be used.");
        }

        // Set up custom enchants
        CreateCustomEnchant createCustomEnchant = new CreateCustomEnchant(this);

        // Load custom configs
        loadEnchantmentsConfig();
        loadSettingsConfig();
        loadMenusConfig();

        // WorldGuard
        worldGuardPlugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("worldguard");

        // Set up Glowing
        glowingEntities = new GlowingEntities(this);

        //Custom Enchant Data
        customEnchantKeys = new NamespacedKey(this, "Custom_Enchants");
        customShardKeys = new NamespacedKey(this, "Custom_Shards");
        customOracleKeys = new NamespacedKey(this, "Custom_Oracle");
        //Spawner Data
        spawnerKeys = new NamespacedKey(this, "Spawners");
        // Initialize PlayerPlacedBlocksManager
        logsPlacedManager = new LogsPlacedManager(this);
        // Initialize PlayerPlacedBlocksManager
        oresPlacedManager = new OresPlacedManager(this);
        // Initialize BlockUtils instance
        blockRadiusFinder = new BlockRadiusFinder(this);
        // Register experienceManager
        experienceManager = new ExperienceManager(this);


        //All Enchants
        this.getServer().getPluginManager().registerEvents(new Fearsight(this),this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Leech(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Hunter(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Rush(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Propel(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new FreezingShot(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new BlessingofKnowledge(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new SafeMiner(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new PoisonAspect(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Stunning(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new IceAspect(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Extractor(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new EnergyAbsorption(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Regrowth(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new TreeHugger(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new MinersTouch(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new VeinSeeker(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new WingsOfAegis(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new Asclepius(this), this); // Added By Ghillie (Old Name Growth)
        this.getServer().getPluginManager().registerEvents(new Decapitate(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new FinalGuard(this), this); // Added By Ghillie
        this.getServer().getPluginManager().registerEvents(new PoseidonsBait(this), this); // Added By Ghillie
        //All Events
        this.getServer().getPluginManager().registerEvents(new ApplyCustomEnchant(this), this);
        this.getServer().getPluginManager().registerEvents(new ApplyShard(this), this);
        this.getServer().getPluginManager().registerEvents(new RemoveCustomEnchant(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateCustomEnchant(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateOracle(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateRandomCustomEnchant(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateShard(this), this);
        this.getServer().getPluginManager().registerEvents(new LootTableEvent(this), this);
        this.getServer().getPluginManager().registerEvents(new ArmorEquipListener(this, getBlockedMaterialNames(this)), this);
        this.getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        this.getServer().getPluginManager().registerEvents(new OraclesOfEnchantmentEvent(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        // Register commands
        this.commandRouter = new CommandRouter(this);
        getCommand("atlasenchants").setExecutor(commandRouter);
        getCommand("atlasenchants").setTabCompleter(commandRouter);

        // BStats Info
        int pluginId = 22376; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        // Plugin Started Message
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e1.3.3"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &aEnabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        glowingEntities.disable();

        // Save data to file on plugin disable
        logsPlacedManager.saveDataToFile();
        // Save data to file on plugin disable
        oresPlacedManager.saveDataToFile();

        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e1.3.3"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &4Disabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
    }

    public ExperienceManager getExperienceManager(){
        return experienceManager;
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

    public WorldGuardPlugin getWorldGuardPlugin()
    {
        if(checkForWorldGuardAPI()){
            return worldGuardPlugin;
        } else {
            return null;
        }
    }

    private boolean checkForPlaceholderAPI() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        return plugin != null && plugin.isEnabled();
    }

    private boolean checkForWorldGuardAPI() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("worldguard");
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

    public FileConfiguration getMenusConfig() {
        return menusConfig;
    }

    public void saveMenusConfig() {
        try {
            menusConfig.save(menusConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMenusConfig() {
        menusConfigFile = new File(getDataFolder(), "menus.yml");
        if (!menusConfigFile.exists()) {
            saveResource("menus.yml", false);
        }
        menusConfig = YamlConfiguration.loadConfiguration(menusConfigFile);
    }

    public void openEnchantListGUI(Player player){
        EnchantListGUI enchantListGUI = new EnchantListGUI(this, player);
        enchantListGUI.open();
    }

    public void openEnchantRarityListGUI(Player player, String rarity){
        EnchantRarityListGUI enchantRarityListGUI = new EnchantRarityListGUI(this, player, rarity);
        enchantRarityListGUI.open();
    }

    public void openUpgradeEnchantGUI(Player player){
        UpgradeEnchantGUI upgradeEnchantGUI = new UpgradeEnchantGUI(this, player);
        upgradeEnchantGUI.open();
    }

    public void openUpgradeRewardGUI(Player player, String rarity){
        UpgradeRewardGUI upgradeRewardGUI = new UpgradeRewardGUI(this, player, rarity);
        upgradeRewardGUI.open();
    }

    private static List<String> getBlockedMaterialNames(Main main) {
        try (InputStream inputStream = main.getResource("armorequipevent-blocked.txt")) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines().collect(Collectors.toList());
            } catch (Exception ignored1) {

            }
        } catch (IOException ignored2) {
            //e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static String getRarityColorCode(Main main, String rarity) {
        String path = "EnchantList-Gui.RarityList-Menu.RarityList-Menu-Rarities." + rarity.toUpperCase() + ".Color";
        String color = main.getMenusConfig().getString(path);
        return color != null ? color : "f"; // fallback to white
    }

    public String applyPlaceholders(String input, Main main, String enchantmentName, int enchantmentLevel) {
        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{lvl}", String.valueOf(enchantmentLevel));
        placeholders.put("{blacklistEnchant}", String.valueOf(main.getEnchantmentsConfig().getStringList("Enchantments." + enchantmentName + ".Enchantment-Blacklist-Enchants")));
        placeholders.put("{glowRange}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Radius-of-glowing-" + enchantmentLevel)));
        placeholders.put("{time}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Time-underwater-" + enchantmentLevel)));
        placeholders.put("{hunterDamageAmount}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Hunter-Damage-Amount-" + enchantmentLevel)));
        placeholders.put("{speedLvl}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Rush-Speed-Amount-" + enchantmentLevel)));
        placeholders.put("{speedTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Rush-Speed-Timer-" + enchantmentLevel)));
        placeholders.put("{propelBlockDistance}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Propel-Height-Amount-" + enchantmentLevel)));
        placeholders.put("{freezingTimer}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FreezingShot-Freeze-Timer-" + enchantmentLevel)));
        placeholders.put("{healthTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".HealthBar-Timer-" + enchantmentLevel)));
        placeholders.put("{extraHearts}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Growth-Heart-Increase-" + enchantmentLevel)));
        placeholders.put("{poisonTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-Poison-Timer-" + enchantmentLevel)));
        placeholders.put("{poisonLevel}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-Poison-Level-" + enchantmentLevel)));
        placeholders.put("{stunTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Stunning-Stun-Timer-" + enchantmentLevel)));
        placeholders.put("{stunLevel}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Stunning-Stun-Levels-" + enchantmentLevel)));
        placeholders.put("{iceTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".IceAspect-Frozen-Timer-" + enchantmentLevel)));
        placeholders.put("{extractorMultiplier}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Extractor-EXP-Multiplier-" + enchantmentLevel)));
        placeholders.put("{healingAmount}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".EnergyAbsorption-Healing-Amount-" + enchantmentLevel)));
        placeholders.put("{healingStartAmount}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".EnergyAbsorption-Start-Healing-Amount-" + enchantmentLevel)));
        placeholders.put("{wingsDamageReduction}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".WingsOfAegis-Protection-Percentage-" + enchantmentLevel)));
        placeholders.put("{AsclepiusHearts}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Asclepius-HealthBoost-" + enchantmentLevel) * 2));
        placeholders.put("{decapitateChance}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Decapitate-Proc-Chance-" + enchantmentLevel) * 100));
        placeholders.put("{finalGuardProtectionPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FinalGuard-Protection-Percent-" + enchantmentLevel) * 100));
        placeholders.put("{finalGuardRepairPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FinalGuard-Repair-Percent-" + enchantmentLevel) * 100));
        placeholders.put("{BaitChance}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".PoseidonsBait-Proc-Chance-" + enchantmentLevel) * 100));
        placeholders.put("{leechPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Leech-Healing-Amount-Percent-" + enchantmentLevel) * 100));

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }

        return input;
    }

}
