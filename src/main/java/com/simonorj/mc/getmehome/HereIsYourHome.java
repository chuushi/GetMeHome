package com.simonorj.mc.getmehome;

import java.text.MessageFormat;
import java.util.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public final class HereIsYourHome extends JavaPlugin {
    private HomeStorage storage = null;
    private List<HomeLimit> homeLimit;
    private int defaultLimit;

    private final class HomeLimit {
        private final String permission;
        private final int limit;
        HomeLimit(String permission, int limit) {
            this.permission = permission;
            this.limit = limit;
        }
        public String getPermission() {
            return permission;
        }

        public int getLimit() {
            return limit;
        }

    }

    @Override
    public void onEnable() {
        // Get config
        saveDefaultConfig();
        loadConfiguration();

        loadStorage();

        getServer().getPluginManager().registerEvents(new SavingDetector(), this);
    }

    @Override
    public void onDisable() {
        if (storage != null)
            storage.onDisable();
    }

    private void loadConfiguration() {
        homeLimit = new ArrayList<>();

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
                homeLimit.add(new HomeLimit(s, csl.getInt(s)));
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
                    storage = new HomeSQL(this);
                    return;
                }
            } else if (!type.equalsIgnoreCase("yaml")) {
                    getLogger().warning("storage.type contains illegal type. Using YAML storage method.");
            }

            storage = new HomeYAML(this);
        }
    }

    private Location getHome(Player p, String n) {
        return storage.getHome(p, n);
    }

    private boolean setHome(Player p, String n) {
        if (reachedLimit(p, true)
                || (reachedLimit(p, false) && storage.getHome(p, n) == null)
                || !storage.setHome(p, n))
            return false;

        storage.setHome(p, n);
        return true;
    }

    private boolean deleteHome(Player p, String n) {
        return storage.deleteHome(p, n);
    }

    // exclusive: excludes currently set home from count
    private boolean reachedLimit(Player p, boolean exclusive) {
        // default maximum homes
        int maxHomes = getSetLimit(p);

        // Get current homes
        int setHomes = listHomes(p).size();

        return exclusive ? setHomes > maxHomes
                : setHomes >= maxHomes;
    }

    private int getSetLimit(Player p) {
        // Override if has permission node
        for (HomeLimit l : homeLimit) {
            if (p.hasPermission(l.getPermission())) {
                return l.getLimit();
            }
        }
        return defaultLimit;
    }

    private Map<String, Location> listHomes(Player p) {
        return storage.getAllHomes(p);
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

            if (args[0].equalsIgnoreCase("ClearCache")) {
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
                    if (storage instanceof HomeYAML) {
                        if (args.length > 1 && args[1].equalsIgnoreCase("yes")) {
                            if (storage != null)
                                storage.onDisable();
                        } else if (args.length == 1 || !args[1].equalsIgnoreCase("no")) {
                            TextComponent msg = new TextComponent("Save the storage data to homes.yml? ");
                            TextComponent c = new TextComponent("[Yes]");
                            c.setColor(ChatColor.GREEN);
                            c.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND
                                    , "/" + label + ' ' + args[0] + ' ' + YES));
                            msg.addExtra(c);
                            msg.addExtra(" ");
                            c = new TextComponent("[No]");
                            c.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND
                                    , "/" + label + ' ' + args[0] + ' ' + NO));
                            c.setColor(ChatColor.RED);
                            msg.addExtra(c);
                            if (sender instanceof Player)
                                ((Player) sender).spigot().sendMessage(tagMsg(msg));
                            else
                                sender.sendMessage(tagMsg(msg).toPlainText());
                            return true;
                        }
                    } else {
                        storage.onDisable();
                    }
                    // Delete cache

                    loadStorage();
                }
                // Error occured
                if (loadError != null) {
                    sender.sendMessage(tagMsg(ERROR));
                    for (String s : loadError) {
                        sender.sendMessage(ChatColor.RED + ">" + s);
                    }
                    loadError = null;
                } else {
                    sender.sendMessage(tagMsg(new StringBuilder(ChatColor.GREEN.toString()).append(
                            rc ? "Configuration file"
                                    : rs ? "Storage"
                                    : "All settings"
                    ).append(" reloaded successfully.").toString()));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("somethingelse")) {
                sender.sendMessage(TAG_COLOR + "something, you say?");
                return true;
            }
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("GetMeHome: You must be a player.");
            return true;
        }

        // Player-only area (sethome, delhome, listhomes, home)
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("listhomes")) {
            // Get the homes
            Map<String, Location> map = storage.getAllHomes(p);

            // Emptiness
            if (map == null || map.size() == 0) {
                p.sendMessage(tagMsg(NOHOME));
                return true;
            }

            TextComponent msg = new TextComponent(map.size() == 1 ? LIST : LIST_PL);
            msg.addExtra(" ");

            Set<String> homes = map.keySet();
            for (String n : homes) {
                TextComponent tc = new TextComponent("[" + n + "]");
                tc.setColor(LIST_COLOR);
                tc.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/home " + n));
                msg.addExtra(tc);
                msg.addExtra(" ");
            }

            p.spigot().sendMessage(tagMsg(msg));
            return true;
        }

        // Home name!
        String home = "default";
        if (args.length != 0)
            home = args[0];

        // TODO: Put it in own method
        if (cmd.getName().equalsIgnoreCase("home")) {
            Location loc = getHome(p, home);
            // No home
            if (loc == null && !hasSendError(sender)) {
                p.sendMessage(tagMsg(MessageFormat.format(DNE, home)));
                return true;
            }

            // Safety check
            // TODO: Make safety check toggleable via configuration
            Block h = loc.getBlock().getRelative(BlockFace.UP);
            Block m = loc.getBlock();
            Block b = loc.getBlock().getRelative(BlockFace.DOWN);
            if (h.getType().isOccluding() // TODO: Include lava as well
                    || (m.isLiquid() && !m.getType().equals(Material.STATIONARY_WATER))
                    || b.getType().equals(Material.AIR) || b.isLiquid()) {
                if (args.length > 1 && args[1].equalsIgnoreCase("yes"))
                    p.sendMessage(TAG_COLOR + "> " + UNSAFE_FORCE);
                else {
                    p.sendMessage(tagMsg(MessageFormat.format(UNSAFE, home)));

                    TextComponent msg = new TextComponent("> ");
                    msg.setColor(TAG_COLOR);
                    msg.addExtra(UNSAFE_PROMPT);
                    msg.addExtra(" ");
                    TextComponent yes = new TextComponent('[' + YES + ']');
                    yes.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, '/' + label + ' ' + home + " yes"));
                    yes.setColor(ChatColor.LIGHT_PURPLE);
                    msg.addExtra(yes);
                    p.spigot().sendMessage(msg);
                    return true;
                }
            }
            // Welcome home!
            int dist = new Double(loc.distanceSquared(p.getLocation())).intValue();
            p.teleport(loc);
            boolean farAway = true;
            if (p.getWorld().equals(loc.getWorld()))
                farAway = dist > HOME_DIST;

            if (farAway)
                p.sendMessage(tagMsg(MessageFormat.format(HOME, home)));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("sethome")) {
            if (setHome(p, home))
                p.sendMessage(tagMsg(MessageFormat.format(SET, home)));
            else if (!hasSendError(p))
                p.sendMessage(tagMsg(MessageFormat.format(LIMIT_REACHED, getSetLimit(p))));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("deletehome")) {
            if (deleteHome(p, home))
                p.sendMessage(tagMsg(MessageFormat.format(DELETE, home)));
            else if (!hasSendError(p))
                p.sendMessage(tagMsg(MessageFormat.format(DNE, home)));
            return true;
        }

        return false;
    }

    public final class SavingDetector implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void allOut(PlayerQuitEvent e) {
            if (getServer().getOnlinePlayers().size() <= 1
                    && !playerAllCached.isEmpty()) {
                // Save home data
                storage.onDisable();
                loadStorage();
            }
        }
    }
}
