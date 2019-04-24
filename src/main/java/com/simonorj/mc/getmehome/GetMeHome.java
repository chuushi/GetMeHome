package com.simonorj.mc.getmehome;

import com.google.common.base.Charsets;
import com.simonorj.mc.getmehome.command.HomeCommands;
import com.simonorj.mc.getmehome.command.ListHomesCommand;
import com.simonorj.mc.getmehome.command.MetaCommand;
import com.simonorj.mc.getmehome.storage.HomeStorageAPI;
import com.simonorj.mc.getmehome.storage.StorageYAML;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class GetMeHome extends JavaPlugin {
    private static GetMeHome instance;
    private HomeStorageAPI storage;
    private List<HomePermissionLimit> homePermissionLimit;
    private List<WarmupDelay> warmupDelay;
    private int defaultLimit;
    private long defaultWarmup;
    private boolean warmupWhenHomeOther;

    private String prefix;
    private ChatColor focusColor;
    private ChatColor contentColor;
    private int welcomeHomeRadiusSquared;

    public static GetMeHome getInstance() {
        return instance;
    }

    public int getWelcomeHomeRadiusSquared() {
        return welcomeHomeRadiusSquared;
    }

    String getPrefix() {
        return prefix;
    }

    public ChatColor getFocusColor() {
        return focusColor;
    }

    public ChatColor getContentColor() {
        return contentColor;
    }

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

        if (getConfig().getInt(ConfigTool.CONFIG_VERSION_NODE) != ConfigTool.version)
            saveConfig();

        loadConfig();
        loadStorage();

        getServer().getPluginManager().registerEvents(new SaveListener(), this);

        if (getConfig().getBoolean(ConfigTool.ENABLE_METRICS_NODE, true))
            setupMetrics();
    }

    private void setupMetrics() {
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("prefixBranding", () -> {
            String pre = getConfig().getString(ConfigTool.MESSAGE_PREFIX_NODE, "&6[GetMeHome]");
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

        this.prefix = null;
        this.homePermissionLimit = null;
        this.storage = null;
        GetMeHome.instance = null;
    }

    @Override
    public void saveConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        try {
            //noinspection ResultOfMethodCallIgnored
            configFile.mkdirs();
            String data = ConfigTool.saveToString(getConfig());

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), Charsets.UTF_8)) {
                writer.write(data);
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save config to " + configFile, e);
        }
    }

    public void loadConfig() {
        this.homePermissionLimit = new ArrayList<>();
        this.warmupDelay = new ArrayList<>();

        this.defaultLimit = getConfig().getInt(ConfigTool.LIMIT_DEFAULT_NODE, 1);
        ConfigurationSection csl = getConfig().getConfigurationSection(ConfigTool.LIMIT_ROOT);

        if (csl == null) {
            getLogger().warning("Configuration invalid or missing: " + ConfigTool.LIMIT_ROOT);
        } else {
            for (String s : csl.getKeys(true)) {
                // Skip default and non-number node
                if (s.equals(ConfigTool.DEFAULT_CHILD) || !csl.isInt(s))
                    continue;

                // put it in
                homePermissionLimit.add(new HomePermissionLimit(s, csl.getInt(s)));
            }
        }

        this.defaultWarmup = getConfig().getLong(ConfigTool.WARMUP_DEFAULT_NODE, 0L);
        csl = getConfig().getConfigurationSection(ConfigTool.WARMUP_ROOT);

        if (csl == null) {
            getLogger().warning("Configuration invalid or missing: " + ConfigTool.WARMUP_ROOT);
        } else {
            for (String s : csl.getKeys(true)) {
                // Skip default and non-number node
                if (s.equals(ConfigTool.DEFAULT_CHILD) || !csl.isLong(s))
                    continue;

                // put it in
                warmupDelay.add(new WarmupDelay(s, csl.getLong(s)));
            }
        }

        this.warmupWhenHomeOther = getConfig().getBoolean(ConfigTool.WARMUP_WHEN_HOME_OTHER_NODE, false);
        int whr = getConfig().getInt(ConfigTool.WELCOME_HOME_RADIUS_NODE, 4);
        this.welcomeHomeRadiusSquared = whr * whr;
        this.prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString(ConfigTool.MESSAGE_PREFIX_NODE, "&6[GetMeHome]"));
        this.contentColor = ChatColor.getByChar(getConfig().getString(ConfigTool.MESSAGE_CONTENT_COLOR_NODE, "e"));
        this.focusColor = ChatColor.getByChar(getConfig().getString(ConfigTool.MESSAGE_FOCUS_COLOR_NODE, "f"));
    }

    public HomeStorageAPI getStorage() {
        return storage;
    }

    public int getSetLimit(Player p) {
        // Override if has permission node
        for (HomePermissionLimit l : homePermissionLimit) {
            if (p.hasPermission(l.getPermission())) {
                return l.getLimit();
            }
        }
        return defaultLimit;
    }

    public boolean getWarmupWhenHomeOther() {
        return warmupWhenHomeOther;
    }

    public long getWarmupDelay(Player p) {
        // Override if has permission node
        for (WarmupDelay l : warmupDelay) {
            if (p.hasPermission(l.getPermission())) {
                return l.getDelay();
            }
        }
        return defaultWarmup;
    }

    // TODO: Move all kind of configuration storage logic elsewhere
    private final class HomePermissionLimit {
        private final String permission;
        private final int limit;
        HomePermissionLimit(String permission, int limit) {
            this.permission = permission;
            this.limit = limit;
        }

        String getPermission() {
            return permission;
        }

        int getLimit() {
            return limit;
        }
    }

    private final class WarmupDelay {
        private final String permission;
        private final long delay;
        WarmupDelay(String permission, long delay) {
            this.permission = permission;
            this.delay = delay;
        }

        String getPermission() {
            return permission;
        }

        long getDelay() {
            return delay;
        }
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
