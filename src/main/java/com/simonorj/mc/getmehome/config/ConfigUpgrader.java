package com.simonorj.mc.getmehome.config;

import com.google.common.base.Charsets;
import com.simonorj.mc.getmehome.ConfigTool;
import com.simonorj.mc.getmehome.GetMeHome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.logging.Level;

public class ConfigUpgrader {
    public static void upgradeConfig(GetMeHome plugin) {
        // 3: when limit.yml and delay.yml were created
        if (plugin.getConfig().getInt(ConfigTool.CONFIG_VERSION_NODE) < 3) {
            upTo3();
        }

        plugin.saveConfig();
        GetMeHome.getInstance().getLogger().info("Updating configuration to version " + ConfigTool.version);
    }

    private static void upTo3() {
        final String LIMIT_ROOT = "limit";
        final String DEFAULT = "default";
        final String LIMIT_DEFAULT_NODE = "limit.default";
        final String DEFAULT_LIMIT_NODE = "default.limit";
        GetMeHome pl = GetMeHome.getInstance();

        File limitf = new File(pl.getDataFolder(), "limit.yml");
        YamlConfiguration limitc = YamlConfiguration.loadConfiguration(limitf);

        // Move Home Limits configuration from config.yml to limit.yml
        limitc.set(DEFAULT_LIMIT_NODE, pl.getConfig().getInt(LIMIT_DEFAULT_NODE, 1));
        limitc.set(LIMIT_ROOT, null);

        // Make sure to set header comments
        limitc.options().copyHeader(true);

        // Using traditional method keeps breaking up the permission node-style periods.
        // So I had to build this kind of monstery ;-;
        GetMeHome.getInstance().getLogger().warning("Moving home limits configuration from config.yml to homes.yml...");
        try {
            StringBuilder data = new StringBuilder(limitc.saveToString());
            data.append("\nlimit:\n");

            ConfigurationSection cs = pl.getConfig().getConfigurationSection(LIMIT_ROOT);

            for (String key : cs.getKeys(true)) {
                if (!cs.isInt(key) || key.equals(DEFAULT))
                    continue;

                data.append("- perm: ")
                        .append(key)
                        .append("\n  value: ")
                        .append(cs.getInt(key))
                        .append('\n');
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(limitf), Charsets.UTF_8)) {
                writer.write(data.toString());
            }
        } catch (IOException e) {
            pl.getLogger().log(Level.SEVERE, "Could not update/save home limit information to 'limit.yml'", e);
        }
        GetMeHome.getInstance().getLogger().info("Home limits configuration moved to homes.yml!");
    }
}
