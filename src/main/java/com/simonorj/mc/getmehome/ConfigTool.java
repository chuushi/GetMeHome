package com.simonorj.mc.getmehome;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class ConfigTool {
    private static final String STORAGE_ROOT = "storage";
    private static final String SAVENAME_CHILD = "savename";
    private static final String STORAGE_SAVENAME_NODE_V1 = "storage.storage";
    public static final String STORAGE_SAVENAME_NODE = STORAGE_ROOT + "." + SAVENAME_CHILD;

    private static final String MESSAGE_ROOT = "message";
    private static final String PREFIX_CHILD = "prefix";
    private static final String CONTENT_COLOR_CHILD = "content-color";
    private static final String FOCUS_COLOR_CHILD = "focus-color";
    static final String MESSAGE_PREFIX_NODE = MESSAGE_ROOT + "." + PREFIX_CHILD;
    static final String MESSAGE_CONTENT_COLOR_NODE = MESSAGE_ROOT + "." + CONTENT_COLOR_CHILD;
    static final String MESSAGE_FOCUS_COLOR_NODE = MESSAGE_ROOT + "." + FOCUS_COLOR_CHILD;

    static final String WELCOME_HOME_RADIUS_NODE = "welcome-home-radius";

    static final String LIMIT_ROOT = "limit";
    static final String WARMUP_ROOT = "warmup";
    static final String DEFAULT_CHILD = "default";
    static final String LIMIT_DEFAULT_NODE = LIMIT_ROOT + "." + DEFAULT_CHILD;

    static final String ENABLE_METRICS_NODE = "enable-metrics";
    static final String CONFIG_VERSION_NODE = "config-version";
    static final int version = 3;

    private static final String HEADER =
            "# GetMeHome by Simon Chuu\n" +
            "\n" +
            "# For help, follow the plugin project link below:\n" +
            "# https://github.com/SimonOrJ/GetMeHome/\n";

    private static final String STORAGE =
            "# Storage Settings\n";

    private static final String STORAGE_SAVENAME =
            "  # should the plugin save player names with their UUID?\n" +
            "  # This is only for your reference when looking through the file.\n";

    private static final String MESSAGE =
            "";

    private static final String MESSAGE_PREFIX =
            "  # Prefix for beginning of message. Use & and 0~9, a~f to set colors.\n";

    private static final String MESSAGE_CONTENT_COLOR =
            "  # Colors: Accepted values are 0~9, a~f, or named colors.\n" +
            "  # https://minecraft.gamepedia.com/Formatting_codes\n";

    private static final String MESSAGE_FOCUS_COLOR =
            "  # Color for the focus points. e.g. home names or player names\n";

    private static final String WELCOME_HOME_RADIUS =
            "# Maximum distance away from home point for \"Welcome home\" message to not show\n" +
            "# on /home\n" +
            "#   Set to -1 to disable \"Welcome home\" message\n";

    private static final String LIMIT =
            "# Home limits (based on custom permission nodes)\n" +
            "# Set to -1 for unlimited homes\n";

    private static final String LIMIT_DEFAULT =
            "  # default - default home limit. If removed, defaults to 1.\n";

    private static final String LIMIT_CUSTOM__ =
            "  # The rest of the home limit can be listed in this format:\n" +
            "  #   permission.node: maximum number of homes\n" +
            "  ### Permission on top of this list will be checked first! ###\n";

    private static final String WARMUP =
            "# Home warm-up in ticks (based on custom permission nodes)\n" +
            "# 20 ticks = 1 second\n";

    private static final String WARMUP_DEFAULT =
            "  # default - default warm-up time. If removed, defaults to 0.\n";

    private static final String WARMUP_CUSTOM__ =
            "  # The rest of the warm up time can be listed in this format:\n" +
            "  #   permission.node: warm up time in ticks\n" +
            "  ### Permission on top of this list will be checked first! ###\n";

    private static final String ENABLE_METRICS =
            "# Enable metrics for this plugin? (If unsure, leave it as true)\n" +
            "#   Link to metrics: https://bstats.org/plugin/bukkit/GetMeHome/\n";

    private static final String CONFIG_VERSION =
            "# Keeps track of configuration version -- do not change!\n";

    static String saveToString(FileConfiguration config) {
        boolean storageSavename = config.contains(STORAGE_SAVENAME_NODE_V1)
                ? config.getBoolean(STORAGE_SAVENAME_NODE_V1, true)
                : config.getBoolean(STORAGE_SAVENAME_NODE, true);
        String messagePrefix = config.getString(MESSAGE_PREFIX_NODE, "&6[GetMeHome]");
        String messageContentColor = config.getString(MESSAGE_CONTENT_COLOR_NODE, "e");
        String messageFocusColor = config.getString(MESSAGE_FOCUS_COLOR_NODE, "f");
        int welcomeHomeRadius = config.getInt(WELCOME_HOME_RADIUS_NODE, 4);
        int limitDefault = config.getInt(LIMIT_DEFAULT_NODE, 1);
        boolean metrics = config.getBoolean(ENABLE_METRICS_NODE, true);

        return HEADER +
                '\n' +
                STORAGE +
                STORAGE_ROOT + ":\n" +
                STORAGE_SAVENAME +
                "  " + SAVENAME_CHILD + ": " + storageSavename + '\n' +
                '\n' +
                MESSAGE +
                MESSAGE_ROOT + ":\n" +
                MESSAGE_PREFIX +
                "  " + PREFIX_CHILD + ": \"" + messagePrefix + "\"\n" +
                MESSAGE_CONTENT_COLOR +
                "  " + CONTENT_COLOR_CHILD + ": " + messageContentColor + '\n' +
                MESSAGE_FOCUS_COLOR +
                "  " + FOCUS_COLOR_CHILD + ": " + messageFocusColor + '\n' +
                '\n' +
                WELCOME_HOME_RADIUS +
                WELCOME_HOME_RADIUS_NODE + ": " + welcomeHomeRadius + '\n' +
                '\n' +
                LIMIT +
                LIMIT_ROOT + ":\n" +
                LIMIT_DEFAULT +
                "  " + DEFAULT_CHILD + ": " + limitDefault + '\n' +
                '\n' +
                LIMIT_CUSTOM__ +
                homeLimitsToString(config.getConfigurationSection(LIMIT_ROOT)) +
                "\n\n\n" +
                WARMUP +
                WARMUP_ROOT + ":\n" +
                WARMUP_DEFAULT +
                "  " + DEFAULT_CHILD + ": " + limitDefault + '\n' +
                '\n' +
                WARMUP_CUSTOM__ +
                homeWarmupToString(config.getConfigurationSection(LIMIT_ROOT)) +
                "\n\n\n" +
                ENABLE_METRICS +
                ENABLE_METRICS_NODE + ": " + metrics +
                "\n\n" +
                CONFIG_VERSION +
                CONFIG_VERSION_NODE + ": " + version +
                '\n';
    }

    private static String homeLimitsToString(ConfigurationSection cs) {
        StringBuilder ret = new StringBuilder();
        for (String s : cs.getKeys(true)) {
            // Skip default and non-number node
            if (s.equals(DEFAULT_CHILD) || !cs.isInt(s))
                continue;

            // put it in
            ret.append("  ").append(s).append(": ").append(cs.getInt(s)).append('\n');
        }

        if (ret.length() == 0) {
            ret.append("  ").append("getmehome.op").append(": ").append(-1).append('\n');
            ret.append("  ").append("getmehome.twohomes").append(": ").append(2).append('\n');
        }

        return ret.toString();
    }

    private static String homeWarmupToString(ConfigurationSection cs) {
        StringBuilder ret = new StringBuilder();
        for (String s : cs.getKeys(true)) {
            // Skip default and non-number node
            if (s.equals(DEFAULT_CHILD) || !cs.isInt(s))
                continue;

            // put it in
            ret.append("  ").append(s).append(": ").append(cs.getInt(s)).append('\n');
        }

        if (ret.length() == 0) {
            ret.append("  ").append("getmehome.op").append(": ").append(0).append('\n');
            ret.append("  ").append("getmehome.wait5s").append(": ").append(100).append('\n');
        }

        return ret.toString();
    }
}
