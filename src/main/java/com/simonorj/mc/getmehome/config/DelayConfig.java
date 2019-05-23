package com.simonorj.mc.getmehome.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DelayConfig {
    private static final String DEFAULT_WARMUP_NODE = "default.warmup";
    private static final String DEFAULT_COOLDOWN_NODE = "default.cooldown";
    private static final String WARMUP_PARENT_NODE = "warmup";
    private static final String COOLDOWN_PARENT_NODE = "cooldown";
    private final YamlConfiguration config;
    private final List<PermissionValue> warmup;
    private final List<PermissionValue> cooldown;
    private int defaultWarmup = 1;
    private int defaultCooldown = 1;

    DelayConfig(File file) {
        this.config = YamlConfiguration.loadConfiguration(file);
        this.warmup = new ArrayList<>();
        this.cooldown = new ArrayList<>();
    }

    public int getDefaultWarmup() {
        return defaultWarmup;
    }

    public int getDefaultCooldown() {
        return defaultCooldown;
    }

    public void setup() {
        this.defaultWarmup = config.getInt(DEFAULT_WARMUP_NODE, 0);
        this.defaultCooldown = config.getInt(DEFAULT_COOLDOWN_NODE, 0);
        PermissionValue.parseConfigurationSection(warmup, config.getConfigurationSection(WARMUP_PARENT_NODE));
        PermissionValue.parseConfigurationSection(cooldown, config.getConfigurationSection(COOLDOWN_PARENT_NODE));
    }

}
