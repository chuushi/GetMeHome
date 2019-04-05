package com.simonorj.mc.getmehome.command;

import com.simonorj.mc.getmehome.GetMeHome;
import com.simonorj.mc.getmehome.MessageTool;
import com.simonorj.mc.getmehome.storage.HomeStorage;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

public class HomeCommand implements TabExecutor, MessageTool {
    private static final String OTHER_PERM = "getmehome.command.home.other";
    private final GetMeHome plugin;

    public HomeCommand(GetMeHome plugin) {
        this.plugin = plugin;
    }

    private HomeStorage getStorage() {
        return plugin.getStorage();
    }

    @Override
    public boolean onCommand(CommandSender sender, final Command cmd, String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player.");
            return true;
        }

        final Player p = (Player) sender;

        String home;

        if (args.length != 0) home = args[0];
        else home = getStorage().getDefaultHomeName(p);

        if (cmd.getName().equalsIgnoreCase("home")) {
            Location loc = getStorage().getHome(p, home);
            // No home
            if (loc == null) {
                p.sendMessage(base("commands.generic.homeDoesNotExist", getLocale(p), home));
                return true;
            }

            // Welcome home!
            boolean farAway = true;
            if (p.getWorld() == loc.getWorld()) {
                double dist = loc.distanceSquared(p.getLocation());
                farAway = dist > 25.0;
            }

            p.teleport(loc);

            if (farAway)
                plugin.messageTo(p, localize.getString("commands.home.success"));

            return true;
        }

        if (cmd.getName().equalsIgnoreCase("sethome")) {
            int limit = plugin.getSetLimit(p);
            boolean allow;
            boolean homeExists;
            if (homeExists = getStorage().getHome(p, home) != null)
                allow = limit >= getStorage().getNumberOfHomes(p);
            else
                allow = limit > getStorage().getNumberOfHomes(p);

            if (allow) {
                if (getStorage().setHome(p, home))
                    if (homeExists)
                        plugin.messageTo(p, String.format(localize.getString("commands.sethome.relocate"), home));
                    else plugin.messageTo(p, String.format(localize.getString("commands.sethome.new"), home));
                else
                    plugin.messageTo(p, localize.getString("commands.sethome.badLocation"));
            } else
                plugin.messageTo(p, String.format(localize.getString("commands.sethome.reachedLimit"), String.valueOf(limit)));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("setdefaulthome")) {
            if (getStorage().setDefaultHome(p, home))
                plugin.messageTo(p, String.format(localize.getString("commands.setdefaulthome"), home));
            else
                plugin.messageTo(p, String.format(localize.getString("commands.generic.homeDoesNotExist"), home));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("delhome")) {
            if (getStorage().deleteHome(p, home))
                plugin.messageTo(p, String.format(localize.getString("commands.delhome"), home));
            else
                plugin.messageTo(p, String.format(localize.getString("commands.generic.homeDoesNotExist"), home));
            //return; (implied)
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return Collections.emptyList();

        if (args.length != 1)
            return new ArrayList<>(getStorage().getAllHomes((Player) sender).keySet());

        List<String> ret = new ArrayList<>();

        for (String n : getStorage().getAllHomes((Player) sender).keySet()) {
            if (n.toLowerCase().startsWith(args[0].toLowerCase())) {
                ret.add(n);
            }
        }

        return ret;
    }
}
