package com.simonorj.mc.getmehome.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LimitConfig {
    private static final String DEFAULT_LIMIT_NODE = "default.limit";
    private static final String LIMIT_PARENT_NODE = "limit";
    private final YamlConfiguration config;
    private final List<PermissionValue> limit;
    private int defaultLimit = 1;

    LimitConfig(File file) {
        this.config = YamlConfiguration.loadConfiguration(file);
        this.limit = new ArrayList<>();
    }

    public int getDefault() {
        return defaultLimit;
    }

    public void setup() {
        this.defaultLimit = config.getInt(DEFAULT_LIMIT_NODE, 1);
        PermissionValue.parseConfigurationSection(limit, config.getConfigurationSection(LIMIT_PARENT_NODE));
    }
}
