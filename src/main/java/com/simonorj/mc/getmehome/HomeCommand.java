package com.simonorj.mc.getmehome;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// sethome, delhome, home
public class HomeCommand implements TabExecutor {
    private final GetMeHome plugin;
    private final HomeStorage storage;

    HomeCommand(GetMeHome plugin, HomeStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player!");
            return true;
        }

        Player p = (Player) sender;

        String home;

        if (args.length != 0) home = args[0];
        else home = storage.getDefaultHomeName(p);

        if (cmd.getName().equalsIgnoreCase("home")) {
            Location loc = storage.getHome(p, home);
            // No home
            if (loc == null) {
                p.sendMessage("Home not found!");
                return true;
            }

            // Welcome home!
            int dist = new Double(loc.distanceSquared(p.getLocation())).intValue();
            p.teleport(loc);
            boolean farAway = true;
            if (p.getWorld().equals(loc.getWorld()))
                farAway = dist > 25;

            if (farAway)
                p.sendMessage("Welcome Home.");

            return true;
        }

        if (cmd.getName().equalsIgnoreCase("sethome")) {
            int limit = plugin.getSetLimit(p);
            if (plugin.getSetLimit(p) > storage.getNumberOfHomes(p)) {
                if (storage.setHome(p, home))
                    p.sendMessage("Home set.");
                else
                    p.sendMessage("Home can't be set here.");
            }
            else
                p.sendMessage("Home limit reached. Overwrite old home or delete homes. Your limit: " + limit);
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("setdefaulthome")) {
            if (storage.setDefaultHome(p, home))
                p.sendMessage("Default home set.");
            else
                p.sendMessage("Home not found.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("deletehome")) {
            if (storage.deleteHome(p, home))
                p.sendMessage("Home deleted.");
            else
                p.sendMessage("Home not found.");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return Collections.emptyList();

        if (args.length == 0)
            return new ArrayList<>(storage.getAllHomes((Player)sender).keySet());

        List<String> ret = new ArrayList<>();

        for (String n : storage.getAllHomes((Player)sender).keySet()) {
            if (n.toLowerCase().startsWith(args[0].toLowerCase())) {
                ret.add(n);
            }
        }

        return ret;
    }
}
