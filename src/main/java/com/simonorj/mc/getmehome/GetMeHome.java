package com.simonorj.mc.getmehome;

import com.google.common.base.Charsets;
import com.simonorj.mc.getmehome.command.HomeCommands;
import com.simonorj.mc.getmehome.command.ListHomesCommand;
import com.simonorj.mc.getmehome.command.MetaCommand;
import com.simonorj.mc.getmehome.config.ConfigUpgrader;
import com.simonorj.mc.getmehome.config.YamlPermValue;
import com.simonorj.mc.getmehome.storage.HomeStorageAPI;
import com.simonorj.mc.getmehome.storage.StorageYAML;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;

public final class GetMeHome extends JavaPlugin {
    private static GetMeHome instance;
    private HomeStorageAPI storage;

    private YamlPermValue limit;
    private YamlPermValue warmup;
    private YamlPermValue cooldown;

    private String prefix;
    private ChatColor focusColor;
    private ChatColor contentColor;
    private int welcomeHomeRadiusSquared;

    public static GetMeHome getInstance() { return instance; }
    public int getWelcomeHomeRadiusSquared() { return welcomeHomeRadiusSquared; }
    public YamlPermValue getLimit() { return limit; }
    public YamlPermValue getWarmup() { return warmup; }
    public YamlPermValue getCooldown() { return cooldown; }
    public ChatColor getFocusColor() { return focusColor; }
    public ChatColor getContentColor() { return contentColor; }

    String getPrefix() { return prefix; }

    @Override
    public void onEnable() {
        GetMeHome.instance = this;

        getCommand("getmehome").setExecutor(new MetaCommand());
        HomeCommands hc = new HomeCommands(this);
        getCommand("home").setExecutor(hc);
        getCommand("sethome").setExecutor(hc);
        getCommand("setdefaulthome").setExecutor(hc);
        getCommand("delhome").setExecutor(hc);
        getCommand("listhomes").setExecutor(new ListHomesCommand(this));

        // Get config
        saveDefaultConfig();

        File limitf = new File(getDataFolder(), "limit.yml");
        File delayf = new File(getDataFolder(), "delay.yml");

        if (!limitf.exists()) saveResource("limit.yml", false);
        if (!delayf.exists()) saveResource("delay.yml", false);

        this.limit = new YamlPermValue(YamlConfiguration.loadConfiguration(limitf), "limit");
        this.warmup = new YamlPermValue(YamlConfiguration.loadConfiguration(delayf), "warmup");
        this.cooldown = new YamlPermValue(YamlConfiguration.loadConfiguration(delayf), "cooldown");

        if (getConfig().getInt(ConfigTool.CONFIG_VERSION_NODE) != ConfigTool.version) {
            // 3: when limit.yml and delay.yml were created
            if (getConfig().getInt(ConfigTool.CONFIG_VERSION_NODE) < 3) {
                ConfigUpgrader.upTo3();
            }
            saveConfig();
            GetMeHome.getInstance().getLogger().info("Updating configuration to version " + ConfigTool.version);
        }

        loadConfig();
        loadStorage();

        getServer().getPluginManager().registerEvents(new SaveListener(), this);

        setupMetrics();
    }

    private void setupMetrics() {
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("prefixBranding", () -> {
            String pre = getConfig().getString(ConfigTool.MESSAGE_PREFIX_NODE, "&6[GetMeHome]");
            if (pre.isEmpty())
                return "Empty";
            if (pre.equals("&6[GetMeHome]"))
                return "Unchanged";
            if (pre.toLowerCase().contains("getmehome"))
                return "Modified";
            return "Removed";
        }));
        metrics.addCustomChart(new Metrics.SingleLineChart("totalHomes", getStorage()::totalHomes));
    }

    public void loadStorage() {
        storage = new StorageYAML();
    }

    @Override
    public void onDisable() {
        if (storage != null)
            storage.save();

        this.limit = null;
        this.warmup = null;
        this.cooldown = null;

        this.prefix = null;
        this.storage = null;
        GetMeHome.instance = null;
    }

    @Override
    public void saveConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        File bkup = new File(getDataFolder(), "config.bkup.yml");

        try {
            //noinspection ResultOfMethodCallIgnored
            configFile.mkdirs();

            // Back up config.yml first
            Files.copy(configFile.toPath(), bkup.toPath());

            String data = ConfigTool.saveToString(getConfig());

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), Charsets.UTF_8)) {
                writer.write(data);
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }


    public void loadConfig() {
        File limitf = new File(getDataFolder(), "limit.yml");
        File delayf = new File(getDataFolder(), "delay.yml");

        this.limit = new YamlPermValue(YamlConfiguration.loadConfiguration(limitf), "limit");
        this.warmup = new YamlPermValue(YamlConfiguration.loadConfiguration(delayf), "warmup");
        this.cooldown = new YamlPermValue(YamlConfiguration.loadConfiguration(delayf), "cooldown");

        int whr = getConfig().getInt(ConfigTool.WELCOME_HOME_RADIUS_NODE, 4);
        this.welcomeHomeRadiusSquared = whr * whr;
        this.prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString(ConfigTool.MESSAGE_PREFIX_NODE, "&6[GetMeHome]"));
        this.contentColor = ChatColor.getByChar(getConfig().getString(ConfigTool.MESSAGE_CONTENT_COLOR_NODE, "e"));
        this.focusColor = ChatColor.getByChar(getConfig().getString(ConfigTool.MESSAGE_FOCUS_COLOR_NODE, "f"));
    }

    public HomeStorageAPI getStorage() {
        return storage;
    }

    @Deprecated
    public int getSetLimit(Player p) {
        return limit.calcFor(p).value;
    }

    @Deprecated
    public int getWarmupDelay(Player p) {
        return warmup.calcFor(p).value;
    }

    @SuppressWarnings("deprecation")
    public OfflinePlayer getPlayer(String name) {
        UUID uuid = getStorage().getUniqueID(name);
        if (uuid != null)
            return getServer().getOfflinePlayer(uuid);

        Player p = getServer().getPlayer(name);
        if (p != null)
            return p;

        OfflinePlayer op = getServer().getOfflinePlayer(name);
        if (op.hasPlayedBefore())
            return op;

        return null;
    }
}
