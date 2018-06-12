package com.simonorj.mc.getmehome;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    private Map<String, Integer> homeSetLimit = new HashMap<>();
    private int defaultHomeSetLimit = 1;
    private Map<UUID, HashMap<String, Location>> playerHomeCache;
    private Set<UUID> playerAllCached;

    @Override
    public void onEnable() {
        // Get config
        saveDefaultConfig();

        loadConfiguration();
        loadStorage();

        if (loadError != null) {
            getLogger().warning("Configuration wasn't set properly:");
            for (String s : loadError) {
                getLogger().warning("- " + s);
            }
            getServer().getPluginManager().disablePlugin(this);
        } else
            getServer().getPluginManager().registerEvents(new SavingDetector(), this);

    }

    @Override
    public void onDisable() {
        if (storage != null)
            storage.onDisable();
    }

    private void loadConfiguration() {
        if (!getConfig().contains("limit.default"))
            err.add(getConfig().contains("limit") ? "limit is empty" : "limit.default is not set");
        else {
            ConfigurationSection csl = getConfig().getConfigurationSection("limit");

            // Must be Linked
            for (String s : (LinkedHashSet<String>) csl.getKeys(true)) {
                // Skip "default" entry
                if (s.equals("default") || !csl.isInt(s))
                    continue;
                // put it in
                homeSetLimit.put(s, csl.getInt(s));
            }
            // get default entry
            defaultHomeSetLimit = csl.getInt("default");
        }
    }

    private void clearHomeCache() {
        playerHomeCache = new LinkedHashMap<>();
        playerAllCached = new HashSet<>();
    }

    private void loadStorage() {
        Set<String> err = new HashSet<>();
        clearHomeCache();

        // Storage
        if (!getConfig().contains("storage.type"))
            err.add(getConfig().contains("storage") ? "storage is empty" : "storage.type is not set");
        else {
            ConfigurationSection cs = getConfig().getConfigurationSection("storage");
            String type = cs.getString("type");

            if (type.equalsIgnoreCase("yaml"))
                storage = new HomeYAML(this);
            else if (type.equalsIgnoreCase("mysql")) {
                if (!cs.contains("database"))
                    err.add("storage.database is empty");
                storage = new HomeSQL(this, true);
            } else if (type.equalsIgnoreCase("sqlite")) {
                storage = new HomeSQL(this, false);
            }

            if (storage == null) {
                err.add("storage.type contains illegal type");
            }
        }

        if (err.size() != 0) {
            if (loadError == null)
                loadError = err;
            else
                loadError.addAll(err);
        }
    }

    private String tagMsg(String msg) {
        return TAG + ' ' + msg;
    }

    private TextComponent tagMsg(TextComponent msg) {
        TextComponent tag = new TextComponent(TAG);
        tag.addExtra(" ");
        tag.addExtra(msg);
        return tag;
    }

    private Location getHome(Player p, String n) {
        HashMap<String, Location> pHomes = playerHomeCache.get(p.getUniqueId());

        // Master cache has player and player has home
        if (pHomes != null && pHomes.containsKey(n))
            return pHomes.get(n);


        // Cache doesn't have player
        if (pHomes == null) {
            pHomes = new HashMap<>();
            playerHomeCache.put(p.getUniqueId(), pHomes);
        }

        // get from storage
        Location loc = storage.getHome(p, n);

        // player has the named home
        if (loc != null)
            pHomes.put(n, loc);

        return loc;
    }

    private boolean setHome(Player p, String n) {
        if (reachedLimit(p, true)
                || (reachedLimit(p, false) && storage.getHome(p, n) == null)
                || !storage.setHome(p, n))
            return false;

        // Put it in cache
        playerHomeCache.get(p.getUniqueId()).put(n, p.getLocation());

        return true;
    }

    private boolean deleteHome(Player p, String n) {
        // Delete from storage
        if (!storage.deleteHome(p, n))
            return false;
        // Delete from cache
        HashMap<String, Location> rm = playerHomeCache.get(p.getUniqueId());
        if (rm != null)
            rm.remove(n);
        return true;
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
        for (Map.Entry<String, Integer> e : homeSetLimit.entrySet()) {
            if (p.hasPermission(e.getKey())) {
                return e.getValue();
            }
        }
        return defaultHomeSetLimit;
    }

    private HashMap<String, Location> listHomes(Player p) {
        final UUID u = p.getUniqueId();
        if (playerAllCached.contains(u))
            return playerHomeCache.get(u);

        HashMap<String, Location> r = storage.getAllHomes(p);
        playerHomeCache.put(u, r);
        playerAllCached.add(u);
        return r;
    }

    private boolean hasSendError(CommandSender p) {
        final Exception e = storage.getError();
        if (e == null)
            return false;
        p.sendMessage(tagMsg(ERROR));
        e.printStackTrace();
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("getmehome")) {
            if (args.length == 0) {
                sender.sendMessage(tagMsg(ChatColor.WHITE + "Version " + getDescription().getVersion()));
                sender.sendMessage(TAG_COLOR + "> " + ChatColor.WHITE + "by " + getDescription().getAuthors().get(0));
                // Display help message
                return true;
            }

            if (args[0].equalsIgnoreCase("ClearCache")) {
                sender.sendMessage(tagMsg(ChatColor.WHITE + "Version " + getDescription().getVersion()));
                clearHomeCache();
            }

            // Reload-related
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
            HashMap<String, Location> map = listHomes(p);

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
