package com.simonorj.mc.getmehome;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
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
    private HomeStorage storage;
    private List<HomePermissionLimit> homePermissionLimit;
    private int defaultLimit;

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
        // Get config
        saveDefaultConfig();
        loadConfiguration();
        loadStorage();

        HomeCommand hc = new HomeCommand(this, storage);
        getCommand("home").setExecutor(hc);
        getCommand("sethome").setExecutor(hc);
        getCommand("setdefaulthome").setExecutor(hc);
        getCommand("delhome").setExecutor(hc);
        getCommand("home").setTabCompleter(hc);
        getCommand("sethome").setTabCompleter(hc);
        getCommand("setdefaulthome").setTabCompleter(hc);
        getCommand("delhome").setTabCompleter(hc);

        getServer().getPluginManager().registerEvents(new SavingDetector(), this);
    }

    @Override
    public void onDisable() {
        if (storage != null)
            storage.save();
    }

    private void loadConfiguration() {
        homePermissionLimit = new ArrayList<>();

        if (!getConfig().contains("limit.default") || !getConfig().isInt("limit.default"))
            getLogger().warning("Configuration invalid or missing: limit.default");
        else {
            ConfigurationSection csl = getConfig().getConfigurationSection("limit");

            defaultLimit = csl.getInt("default");

            for (String s : csl.getKeys(true)) {
                // Skip default and non-number node
                if (s.equals("default") || !csl.isInt(s))
                    continue;

                // put it in
                homePermissionLimit.add(new HomePermissionLimit(s, csl.getInt(s)));
            }
        }
    }

    private void loadStorage() {
        if (!getConfig().contains("storage.type"))
            getLogger().warning(getConfig().contains("storage") ? "storage is empty" : "storage.type is not set");
        else {
            ConfigurationSection cs = getConfig().getConfigurationSection("storage");
            String type = cs.getString("type");

            if (type.equalsIgnoreCase("mysql")) {
                if (cs.contains("database")) {
                    getLogger().warning("storage.database is empty. Using YAML storage method.");
                } else {
                    storage = new StorageSQL(this);
                    return;
                }
            } else if (!type.equalsIgnoreCase("yaml")) {
                    getLogger().warning("storage.type contains illegal type. Using YAML storage method.");
            }

            storage = new StorageYAML(this);
        }
    }

    int getSetLimit(Player p) {
        // Override if has permission node
        for (HomePermissionLimit l : homePermissionLimit) {
            if (p.hasPermission(l.getPermission())) {
                return l.getLimit();
            }
        }
        return defaultLimit;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("getmehome")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.WHITE + "GetMeHome Version: " + getDescription().getVersion());
                sender.sendMessage(ChatColor.WHITE + "by " + getDescription().getAuthors().get(0));
                // Display list of commands
                return true;
            }

            if (args[0].equalsIgnoreCase("clearcache")) {
                storage.clearCache();
            }

            // Reload-related
            // TODO: Rewrite this mess
            boolean rc = args[0].equalsIgnoreCase("reloadconfig"),
                    rs = args[0].equalsIgnoreCase("reloadstorage");
            if (args[0].equalsIgnoreCase("reload")
                    || rc || rs) {
                reloadConfig();

                // These are correct.
                if (!rs)
                    loadConfiguration();
                if (!rc) {
                    // MySQL and SQLite should save things right on spot.
                    if (storage instanceof StorageYAML) {
                        if (args.length > 1 && args[1].equalsIgnoreCase("yes")) {
                            if (storage != null)
                                storage.save();
                        } else if (args.length == 1 || !args[1].equalsIgnoreCase("no")) {
                            TextComponent msg = new TextComponent("Save the storage data to homes.yml? ");
                            TextComponent c = new TextComponent("[Yes]");
                            c.setColor(ChatColor.GREEN);
                            c.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND
                                    , "/" + label + " " + args[0] + " yes"));
                            msg.addExtra(c);
                            msg.addExtra(" ");
                            c = new TextComponent("[No]");
                            c.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND
                                    , "/" + label + " " + args[0] + " no"));
                            c.setColor(ChatColor.RED);
                            msg.addExtra(c);
                            if (sender instanceof Player)
                                ((Player) sender).spigot().sendMessage(msg);
                            else
                                sender.sendMessage(msg.toPlainText());
                            return true;
                        }
                    } else {
                        storage.save();
                    }
                    // Delete cache

                    loadStorage();
                }

                sender.sendMessage(new StringBuilder(ChatColor.GREEN.toString()).append(
                        rc ? "Configuration file"
                                : rs ? "Storage"
                                : "All settings"
                ).append(" reloaded successfully.").toString());

                return true;
            }
        }
        return false;
    }

    public final class SavingDetector implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void allOut(PlayerQuitEvent e) {
            if (getServer().getOnlinePlayers().size() <= 1) {
                storage.save();
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void worldSave(WorldSaveEvent e) {
            // Save home data
            storage.save();
        }
    }
}
