package com.simonorj.mc.getmehome.config;

import com.simonorj.mc.getmehome.GetMeHome;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigUpgrader {
    public static void upTo3() {
        final String LIMIT_ROOT = "limit";
        final String LIMIT_DEFAULT_NODE = "limit.default";
        final String DEFAULT_LIMIT_NODE = "default.limit";
        GetMeHome pl = GetMeHome.getInstance();

        File limitf = new File(pl.getDataFolder(), "limit.yml");
        YamlConfiguration limitc = YamlConfiguration.loadConfiguration(limitf);

        // Move Home Limits configuration from config.yml to limit.yml
        limitc.set(DEFAULT_LIMIT_NODE, pl.getConfig().getInt(LIMIT_DEFAULT_NODE, 1));
        limitc.set(LIMIT_ROOT, pl.getConfig().getConfigurationSection(LIMIT_ROOT));
        limitc.set(LIMIT_DEFAULT_NODE, null);

        // TODO: Set Header Comments; see if this works
        limitc.options().copyHeader(true);

        try {
            limitc.save(limitf);
        } catch (IOException e) {
            GetMeHome.getInstance().getLogger().severe("Could not update/save home limit information to \"limit.yml\"!");
        }
    }
}
