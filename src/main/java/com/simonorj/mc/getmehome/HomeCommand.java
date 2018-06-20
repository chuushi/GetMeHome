package com.simonorj.mc.getmehome;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

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
        String[] temp = p.spigot().getLocale().split("_");
        ResourceBundle localize = ResourceBundle.getBundle("GetMeHome", new Locale(temp[0], temp[1]));

        String home;

        if (args.length != 0) home = args[0];
        else home = storage.getDefaultHomeName(p);

        if (cmd.getName().equalsIgnoreCase("home")) {
            Location loc = storage.getHome(p, home);
            // No home
            if (loc == null) {
                p.sendMessage(String.format(localize.getString("commands.delhome.doesNotExist"), home));
                return true;
            }

            // Welcome home!
            int dist = new Double(loc.distanceSquared(p.getLocation())).intValue();
            p.teleport(loc);
            boolean farAway = true;
            if (p.getWorld().equals(loc.getWorld()))
                farAway = dist > 25;

            if (farAway)
                p.sendMessage(localize.getString("commands.home.success"));

            return true;
        }

        if (cmd.getName().equalsIgnoreCase("sethome")) {
            int limit = plugin.getSetLimit(p);
            boolean allow;
            boolean homeExists;
            if (homeExists = storage.getHome(p, home) != null)
                allow = limit >= storage.getNumberOfHomes(p);
            else
                allow = limit > storage.getNumberOfHomes(p);

            if (allow) {
                if (storage.setHome(p, home))
                    if (homeExists) p.sendMessage(String.format(localize.getString("commands.sethome.relocate"), home));
                    else p.sendMessage(String.format(localize.getString("commands.sethome.new"), home));
                else
                    p.sendMessage(localize.getString("commands.sethome.badLocation"));
            }
            else
                p.sendMessage(String.format(localize.getString("commands.sethome.reachedLimit"), String.valueOf(limit)));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("setdefaulthome")) {
            if (storage.setDefaultHome(p, home))
                p.sendMessage(String.format(localize.getString("commands.setdefaulthome"), home));
            else
                p.sendMessage(String.format(localize.getString("commands.generic.homeDoesNotExist"), home));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("deletehome")) {
            if (storage.deleteHome(p, home))
                p.sendMessage(String.format(localize.getString("commands.delhome"), home));
            else
                p.sendMessage(String.format(localize.getString("commands.generic.homeDoesNotExist"), home));
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
