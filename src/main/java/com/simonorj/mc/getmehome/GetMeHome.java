package com.simonorj.mc.getmehome;

import com.simonorj.mc.getmehome.command.HomeCommand;
import com.simonorj.mc.getmehome.command.ListHomesCommand;
import com.simonorj.mc.getmehome.command.MetaCommand;
import com.simonorj.mc.getmehome.storage.HomeStorage;
import com.simonorj.mc.getmehome.storage.StorageYAML;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class GetMeHome extends JavaPlugin {
    private static GetMeHome instance;
    private static ChatColor messageColor;

    private HomeStorage storage;
    private List<HomePermissionLimit> homePermissionLimit;
    private int defaultLimit;

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

        // Get config
        saveDefaultConfig();
        loadConfiguration();
        loadStorage();

        getCommand("getmehome").setExecutor(new MetaCommand());
        HomeCommand hc = new HomeCommand(this);
        getCommand("home").setExecutor(hc);
        getCommand("sethome").setExecutor(hc);
        getCommand("setdefaulthome").setExecutor(hc);
        getCommand("delhome").setExecutor(hc);
        getCommand("listhomes").setExecutor(new ListHomesCommand(this));

        getServer().getPluginManager().registerEvents(new SavingDetector(), this);
    }

    @Override
    public void onDisable() {
        if (storage != null)
            storage.save();
    }

    private void loadConfiguration() {
        homePermissionLimit = new ArrayList<>();

        defaultLimit = getConfig().getInt("limit.default", 1);
        ConfigurationSection csl = getConfig().getConfigurationSection("limit");

        if (csl == null) {
            getLogger().warning("Configuration invalid or missing: limit");
            return;
        }


        for (String s : csl.getKeys(true)) {
            // Skip default and non-number node
            if (s.equals("default") || !csl.isInt(s))
                continue;

            // put it in
            homePermissionLimit.add(new HomePermissionLimit(s, csl.getInt(s)));
        }

        GetMeHome.messageColor = ChatColor.getByChar(getConfig().getString("formatting.color", "e").charAt(0));
    }

    private void loadStorage() {
        storage = new StorageYAML();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return false;
    }

    public HomeStorage getStorage() {
        return storage;
    }

    public static ChatColor getMessageColor() {
        return messageColor;
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

    public final class SavingDetector implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onAllQuit(PlayerQuitEvent e) {
            if (getServer().getOnlinePlayers().size() <= 1) {
                storage.save();
                storage.clearCache();
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void worldSave(WorldSaveEvent e) {
            // Save home data
            storage.save();
        }
    }
}
