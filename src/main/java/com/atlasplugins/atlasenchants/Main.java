package com.atlasplugins.atlasenchants;

import com.atlasplugins.atlasenchants.commands.CommandRouter;
import com.atlasplugins.atlasenchants.enchants.armor.*;
import com.atlasplugins.atlasenchants.enchants.defense.EnergyAbsorption;
import com.atlasplugins.atlasenchants.enchants.defense.FinalGuard;
import com.atlasplugins.atlasenchants.enchants.tools.*;
import com.atlasplugins.atlasenchants.enchants.weapons.*;
import com.atlasplugins.atlasenchants.guis.*;
import com.atlasplugins.atlasenchants.listeners.CauldronEvent;
import com.atlasplugins.atlasenchants.listeners.OraclesOfEnchantmentEvent;
import com.atlasplugins.atlasenchants.listeners.AltarOfCirceCraftingRecipe;
import com.atlasplugins.atlasenchants.listeners.AltarOfCirceEvent;
import com.atlasplugins.atlasenchants.listeners.CircesAnvilCraftingRecipe;
import com.atlasplugins.atlasenchants.listeners.CircesAnvilEvent;
import com.atlasplugins.atlasenchants.listeners.CircesBrandEvent;
import com.atlasplugins.atlasenchants.listeners.enchantevents.*;
import com.atlasplugins.atlasenchants.listeners.armorevents.ArmorEquipListener;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import fr.skytasul.glowingentities.GlowingEntities;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public final class Main extends JavaPlugin implements Listener {

    public static Main instance;

    // Change chat colors and resolve {header}/{footer} placeholders
    public static String color(String string) {
        if (instance != null && instance.getSettingsConfig() != null) {
            String header = instance.getSettingsConfig().getString("Header", "");
            String footer = instance.getSettingsConfig().getString("Footer", "");
            string = string.replace("{header}", header);
            string = string.replace("{footer}", footer);
        }
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    // Resolved config material names, so a name is only looked up once no matter how hot the
    // call site is (these sit in inventory-click handlers that fire for every click a player makes).
    private static final Map<String, Material> materialCache = new HashMap<>();
    // Names already warned about, so a single typo doesn't produce one console line per click.
    private static final Set<String> warnedMaterials = new HashSet<>();

    // Resolve a Material from a config string. Uses matchMaterial rather than valueOf so that
    // lowercase and namespaced ("minecraft:book") names work, and returns the fallback instead of
    // throwing when the name is wrong - a typo in config should not break every inventory click.
    public static Material getMaterial(String name, Material fallback) {
        if (name == null || name.isEmpty()) return fallback;

        Material cached = materialCache.get(name);
        if (cached != null) return cached;

        Material resolved = Material.matchMaterial(name);
        if (resolved == null) {
            if (instance != null && warnedMaterials.add(name)) {
                instance.getLogger().warning("Unknown material name in config: '" + name
                        + "' - falling back to " + fallback + ".");
            }
            return fallback;
        }

        materialCache.put(name, resolved);
        return resolved;
    }

    // Resolve a Sound from a config string. Sound became an interface in 1.21+, but
    // it still exposes a static valueOf(String) that maps the legacy enum-style names
    // (e.g. "BLOCK_ANVIL_USE") to the right registry entry. We wrap it so a typo'd
    // sound name in config logs a warning and is skipped instead of throwing.
    public static Sound getSound(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException e) {
            if (instance != null) {
                instance.getLogger().warning("Unknown sound name in config: '" + name + "' - it will be skipped.");
            }
            return null;
        }
    }

    // TESTING ONLY: log a spawn/odds roll to the server console (NOT player chat) so the odds
    // are visible while testing. Gated behind EnchantItems.EnchantItem-Debug-Odds. Shared by the
    // loot-chest spawner and the Wandering Trader so both report through one format.
    // 'source' = where the roll happened (Loot/Trader), 'who' = who/what triggered it.
    public void debugOddsRoll(String source, String who, String label, double roll, double chance, boolean passed) {
        if (!getSettingsConfig().getBoolean("EnchantItems.EnchantItem-Debug-Odds")) return;
        String msg = color(String.format(
                "&8[&bAE Debug&8] &7(%s) &f%s &7by &e%s&7: roll &e%.4f &7vs chance &e%.4f &7-> %s",
                source, label, who, roll, chance, passed ? "&aPASS" : "&cFAIL"));
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    // TESTING ONLY: same gate as debugOddsRoll, for lines that report an outcome rather than a
    // roll (e.g. which enchant the Altar finally handed out).
    public void debugOddsInfo(String source, String who, String message) {
        if (!getSettingsConfig().getBoolean("EnchantItems.EnchantItem-Debug-Odds")) return;
        Bukkit.getConsoleSender().sendMessage(color(String.format(
                "&8[&bAE Debug&8] &7(%s) &7by &e%s&7: &f%s", source, who, message)));
    }

    // Glowing Stuff
    public GlowingEntities glowingEntities;

    // Enchantment Stuff
    public static NamespacedKey customEnchantKeys;
    public static NamespacedKey customShardKeys;
    public static NamespacedKey customOracleBookKeys;
    public static NamespacedKey customAltarOfCirceKeys;
    public static NamespacedKey customScrapOfCirceKeys;
    public static NamespacedKey customCircesEmberKeys;
    public static NamespacedKey customCircesAnvilKeys;
    public static NamespacedKey customCircesBrandKeys;
    // Spawner Stuff
    public static NamespacedKey spawnerKeys;

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

        // Suppress vanilla "Named entity ... died: ..." console spam caused by
        // enchants (e.g. Blessing of Knowledge) that name mobs to show a health bar.
        com.atlasplugins.atlasenchants.utils.NamedEntityLogFilter.install();

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
        customOracleBookKeys = new NamespacedKey(this, "Custom_Oracle_Books");
        customAltarOfCirceKeys = new NamespacedKey(this, "Custom_Oracle_Table");
        customScrapOfCirceKeys = new NamespacedKey(this, "Custom_ScrapOfCirce");
        customCircesEmberKeys = new NamespacedKey(this, "Custom_CircesEmber");
        customCircesAnvilKeys = new NamespacedKey(this, "Custom_CircesAnvil");
        customCircesBrandKeys = new NamespacedKey(this, "Custom_CircesBrand");
        //Spawner Data
        spawnerKeys = new NamespacedKey(this, "Spawners");


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
        this.getServer().getPluginManager().registerEvents(new CreateOracleBook(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateAltarOfCirce(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateRandomCustomEnchant(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateScrapOfCirce(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateShard(this), this);
        this.getServer().getPluginManager().registerEvents(new LootTableEvent(this), this);
        this.getServer().getPluginManager().registerEvents(new ArmorEquipListener(this, getBlockedMaterialNames(this)), this);
        this.getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        this.getServer().getPluginManager().registerEvents(new OraclesOfEnchantmentEvent(this), this);
        this.getServer().getPluginManager().registerEvents(new AltarOfCirceEvent(this), this);
        this.getServer().getPluginManager().registerEvents(new WanderingTraderEvent(this), this);
        this.getServer().getPluginManager().registerEvents(new CauldronEvent(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateCircesEmber(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateCircesAnvil(this), this);
        this.getServer().getPluginManager().registerEvents(new CircesAnvilEvent(this), this);
        this.getServer().getPluginManager().registerEvents(new CreateCircesBrand(this), this);
        this.getServer().getPluginManager().registerEvents(new CircesBrandEvent(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        // Crafting Recipe
        new AltarOfCirceCraftingRecipe(this).registerAltarOfCirceRecipe();
        boolean isCircesAnvilCraftingEnabled = getEnchantmentsConfig().getBoolean("CircesAnvil.CircesAnvil-Crafting-Enabled", true);
        if (isCircesAnvilCraftingEnabled) {
            new CircesAnvilCraftingRecipe(this).registerCircesAnvilRecipe();
        }

        // Register commands
        this.commandRouter = new CommandRouter(this);
        getCommand("atlasenchants").setExecutor(commandRouter);
        getCommand("atlasenchants").setTabCompleter(commandRouter);

        // BStats Info
        int pluginId = 22376; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        // Plugin Started Message
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e" + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &aEnabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        com.atlasplugins.atlasenchants.utils.NamedEntityLogFilter.uninstall();

        glowingEntities.disable();

        // Clean up GUI tracking maps to prevent memory leaks on reload
        GuiManager.cleanupAll();

        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
        Bukkit.getConsoleSender().sendMessage(color("&7&l[&c&lAtlas Enchants&7&l] &e" + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cMade by _Ghillie"));
        Bukkit.getConsoleSender().sendMessage(color(""));
        Bukkit.getConsoleSender().sendMessage(color("&cPlugin &4Disabled"));
        Bukkit.getConsoleSender().sendMessage(color("&4---------------------"));
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

    public void loadEnchantmentsConfig() {
        enchantmentsConfigFile = new File(getDataFolder(), "enchantments.yml");
        if (!enchantmentsConfigFile.exists()) {
            saveResource("enchantments.yml", false);
        }
        enchantmentsConfig = YamlConfiguration.loadConfiguration(enchantmentsConfigFile);
        mergeDefaults(enchantmentsConfig, "enchantments.yml", enchantmentsConfigFile);
    }

    // Fill in any keys missing from an existing config file using the bundled default
    // resource, preserving the user's existing values. Prevents null-key crashes when a
    // config from an older plugin version is missing newly-added keys.
    private void mergeDefaults(FileConfiguration config, String resourceName, File file) {
        try (InputStream defStream = getResource(resourceName)) {
            if (defStream == null) return;
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            config.setDefaults(defConfig);
            config.options().copyDefaults(true);
            config.save(file);
        } catch (IOException e) {
            getLogger().warning("Could not merge default keys into " + resourceName + ": " + e.getMessage());
        }
    }

    public FileConfiguration getSettingsConfig() {
        return settingsConfig;
    }

    public void loadSettingsConfig() {
        settingsConfigFile = new File(getDataFolder(), "settings.yml");
        if (!settingsConfigFile.exists()) {
            saveResource("settings.yml", false);
        }
        settingsConfig = YamlConfiguration.loadConfiguration(settingsConfigFile);
        mergeDefaults(settingsConfig, "settings.yml", settingsConfigFile);
    }

    public FileConfiguration getMenusConfig() {
        return menusConfig;
    }

    public void loadMenusConfig() {
        menusConfigFile = new File(getDataFolder(), "menus.yml");
        if (!menusConfigFile.exists()) {
            saveResource("menus.yml", false);
        }
        menusConfig = YamlConfiguration.loadConfiguration(menusConfigFile);
        mergeDefaults(menusConfig, "menus.yml", menusConfigFile);
    }

    public void openEnchantListGUI(Player player){
        EnchantListGUI enchantListGUI = new EnchantListGUI(this, player);
        enchantListGUI.open();
    }

    public void openEnchantRarityListGUI(Player player, String rarity){
        EnchantRarityListGUI enchantRarityListGUI = new EnchantRarityListGUI(this, player, rarity);
        enchantRarityListGUI.open();
    }

    public void openGuideGUI(Player player){
        GuideGUI guideGUI = new GuideGUI(this, player);
        guideGUI.open();
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
        }
        return new ArrayList<>();
    }

    public static String getRarityColorCode(Main main, String rarity) {
        String path = "EnchantList-Gui.RarityList-Menu.Rarities." + rarity.toUpperCase() + ".Color";
        String color = main.getMenusConfig().getString(path);
        return color != null ? color : "f"; // fallback to white
    }

    public String applyPlaceholders(String input, Main main, String enchantmentName, int enchantmentLevel) {
        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{lvl}", String.valueOf(enchantmentLevel));
        placeholders.put("{blacklistEnchant}", String.valueOf(main.getEnchantmentsConfig().getStringList("Enchantments." + enchantmentName + ".Enchantment-Blacklist-Enchants")));
        placeholders.put("{glowRange}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Fearsight-GlowRadius-" + enchantmentLevel)));
        placeholders.put("{hunterDamageAmount}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Hunter-DamageAmount-" + enchantmentLevel)));
        placeholders.put("{speedLvl}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Rush-SpeedAmount-" + enchantmentLevel)));
        placeholders.put("{speedTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Rush-SpeedTimer-" + enchantmentLevel)));
        placeholders.put("{propelBlockDistance}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Propel-HeightAmount-" + enchantmentLevel)));
        placeholders.put("{freezingTimer}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FreezingShot-FreezeTimer-" + enchantmentLevel)));
        placeholders.put("{healthTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".BlessingOfKnowledge-HealthTimer-" + enchantmentLevel)));
        placeholders.put("{poisonTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-PoisonTimer-" + enchantmentLevel)));
        placeholders.put("{poisonLevel}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-PoisonLevel-" + enchantmentLevel)));
        placeholders.put("{stunTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Stunning-StunTimer-" + enchantmentLevel)));
        placeholders.put("{stunLevel}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Stunning-StunLevel-" + enchantmentLevel)));
        placeholders.put("{iceTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".IceAspect-FreezeTimer-" + enchantmentLevel)));
        placeholders.put("{extractorMultiplier}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Extractor-ExpMultiplier-" + enchantmentLevel)));
        placeholders.put("{healingAmount}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".EnergyAbsorption-HealAmount-" + enchantmentLevel)));
        placeholders.put("{healingStartAmount}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".EnergyAbsorption-StartHealAmount-" + enchantmentLevel)));
        placeholders.put("{wingsDefensePoints}", String.valueOf((int) main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".WingsOfAegis-Armor-Defense-Points")));
        placeholders.put("{wingsToughness}", String.valueOf((int) main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".WingsOfAegis-Armor-Toughness")));
        placeholders.put("{AsclepiusHearts}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Asclepius-HealthBoost-" + enchantmentLevel) * 2));
        placeholders.put("{decapitateChance}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Decapitate-ProcChance-" + enchantmentLevel) * 100));
        placeholders.put("{finalGuardProtectionPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FinalGuard-ProtectionChance-" + enchantmentLevel) * 100));
        placeholders.put("{finalGuardRepairPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FinalGuard-RepairPercent-" + enchantmentLevel) * 100));
        placeholders.put("{BaitChance}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".PoseidonsBait-ProcChance-" + enchantmentLevel) * 100));
        placeholders.put("{leechPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Leech-HealPercent-" + enchantmentLevel) * 100));

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }

        return input;
    }

}
