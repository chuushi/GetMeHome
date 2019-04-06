package com.simonorj.mc.getmehome.command;

import com.simonorj.mc.getmehome.GetMeHome;
import com.simonorj.mc.getmehome.storage.HomeStorageAPI;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

import static com.simonorj.mc.getmehome.MessageTool.*;

public class HomeCommands implements TabExecutor {
    // TODO: Implement a way to go to other player's homes
    private static final String OTHER_HOME_PERM = "getmehome.command.home.other";
    private static final String OTHER_SETHOME_PERM = "getmehome.command.sethome.other";
    private static final String OTHER_DELHOME_PERM = "getmehome.command.delhome.other";
    private final GetMeHome plugin;

    public HomeCommands(GetMeHome plugin) {
        this.plugin = plugin;
    }

    private HomeStorageAPI getStorage() {
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
                p.sendMessage(regular("commands.generic.home.failure", p, home));
                return true;
            }

            // Welcome home!
            boolean farAway;
            if (p.getWorld() == loc.getWorld()) {
                double dist = loc.distanceSquared(p.getLocation());
                farAway = dist > plugin.getWelcomeHomeRadiusSquared();
            } else {
                farAway = true;
            }

            p.teleport(loc);

            if (farAway)
                p.sendMessage(regular("commands.home.success", p));

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
                        p.sendMessage(regular("commands.sethome.relocate", p, home));
                    else
                        p.sendMessage(regular("commands.sethome.new", p, home));
                else
                    p.sendMessage(regular("commands.sethome.badLocation", p));
            } else
                p.sendMessage(regular("commands.sethome.reachedLimit", p, limit));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("setdefaulthome")) {
            if (getStorage().setDefaultHome(p, home))
                p.sendMessage(regular("commands.setdefaulthome", p, home));
            else
                p.sendMessage(regular("commands.generic.home.failure", p, home));
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("delhome")) {
            if (getStorage().deleteHome(p, home))
                p.sendMessage(regular("commands.delhome", p, home));
            else
                p.sendMessage(regular("commands.generic.home.failure", p, home));

            return true;
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

        // TODO: With right permission, show player names as well

        return ret;
    }
}
