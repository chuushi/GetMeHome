package com.simonorj.mc.getmehome;

import com.google.common.base.Charsets;
import com.simonorj.mc.getmehome.command.HomeCommand;
import com.simonorj.mc.getmehome.command.ListHomesCommand;
import com.simonorj.mc.getmehome.command.MetaCommand;
import com.simonorj.mc.getmehome.storage.HomeStorage;
import com.simonorj.mc.getmehome.storage.StorageYAML;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class GetMeHome extends JavaPlugin {
    private static GetMeHome instance;
    private HomeStorage storage;
    private List<HomePermissionLimit> homePermissionLimit;
    private int defaultLimit;

    String prefix;

    public static GetMeHome getInstance() {
        return instance;
    }

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

    @Override
    public void onEnable() {
        GetMeHome.instance = this;

        getCommand("getmehome").setExecutor(new MetaCommand());
        HomeCommand hc = new HomeCommand(this);
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
        metrics.addCustomChart(new Metrics.SingleLineChart("totalHomes", () -> storage.totalHomes()));
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
        homePermissionLimit = new ArrayList<>();

        defaultLimit = getConfig().getInt(ConfigTool.LIMIT_DEFAULT_NODE, 1);
        ConfigurationSection csl = getConfig().getConfigurationSection(ConfigTool.LIMIT_ROOT);

        if (csl == null) {
            getLogger().warning("Configuration invalid or missing: " + ConfigTool.LIMIT_ROOT);
            return;
        }


        for (String s : csl.getKeys(true)) {
            // Skip default and non-number node
            if (s.equals(ConfigTool.DEFAULT_CHILD) || !csl.isInt(s))
                continue;

            // put it in
            homePermissionLimit.add(new HomePermissionLimit(s, csl.getInt(s)));
        }
    }

    public HomeStorage getStorage() {
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

}
