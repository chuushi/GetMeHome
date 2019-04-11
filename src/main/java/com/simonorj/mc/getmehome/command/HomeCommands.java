package com.simonorj.mc.getmehome.command;

import com.simonorj.mc.getmehome.GetMeHome;
import com.simonorj.mc.getmehome.storage.HomeStorageAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.simonorj.mc.getmehome.MessageTool.error;
import static com.simonorj.mc.getmehome.MessageTool.prefixed;

public class HomeCommands implements TabExecutor {
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
        OfflinePlayer target;
        boolean otherHome;

        if ((args.length >= 2) && hasOtherPermission(cmd, sender)) {
            target = plugin.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(error("commands.generic.player.notFound", sender));
                return true;
            }
            otherHome = true;
        } else if (sender instanceof Player) {
            target = (Player) sender;
            otherHome = false;
        } else {
            if (cmd.getName().equalsIgnoreCase("delhome"))
                sender.sendMessage("Usage: /delhome <player> <home name>");
            else
                sender.sendMessage("You must be a player");
            return true;
        }

        String home;

        if (args.length >= 2 && otherHome)
            home = args[1];
        else if (args.length != 0)
            home = args[0];
        else
            home = getStorage().getDefaultHomeName(target.getUniqueId());

        if (cmd.getName().equalsIgnoreCase("home"))
            home((Player) sender, target, home);
        else if (cmd.getName().equalsIgnoreCase("sethome"))
            setHome((Player) sender, target, home);
        else if (cmd.getName().equalsIgnoreCase("setdefaulthome"))
            setDefaultHome((Player) sender, home);
        else if (cmd.getName().equalsIgnoreCase("delhome"))
            deleteHome(sender, target, home);
        else
            return false;

        return true;
    }

    private void deleteHome(CommandSender sender, OfflinePlayer target, String home) {
        if (getStorage().deleteHome(target.getUniqueId(), home)) {
            if (sender == target)
                sender.sendMessage(prefixed("commands.delhome.other", sender, target.getName(), home));
            else
                sender.sendMessage(prefixed("commands.delhome", sender, home));
        } else {
            if (sender == target)
                sender.sendMessage(error("commands.generic.home.other.failure", sender, target.getName(), home));
            else
                sender.sendMessage(error("commands.generic.home.failure", sender, home));
        }
    }

    private void home(Player sender, OfflinePlayer target, String home) {
        Location loc = getStorage().getHome(target.getUniqueId(), home);
        // No home
        if (loc == null) {
            if (sender == target)
                sender.sendMessage(error("commands.generic.home.other.failure", sender, target.getName(), home));
            else
                sender.sendMessage(error("commands.generic.home.failure", sender, home));
            return;
        }

        // Welcome home!
        boolean farAway;
        if (sender.getWorld() == loc.getWorld()) {
            double dist = loc.distanceSquared(sender.getLocation());
            farAway = dist > plugin.getWelcomeHomeRadiusSquared();
        } else {
            farAway = true;
        }

        if (sender.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND)) {
            if (farAway) {
                if (sender == target)
                    sender.sendMessage(prefixed("commands.home.other.success", sender, target.getName(), home));
                else
                    sender.sendMessage(prefixed("commands.home.success", sender));
            }
        } else {
            sender.sendMessage(error("commands.home.unable", sender, home));
        }
    }

    private void setHome(Player sender, OfflinePlayer target, String home) {
        int limit = target instanceof Player ? plugin.getSetLimit((Player) target) : -1;
        int current = getStorage().getNumberOfHomes(target.getUniqueId());
        boolean allow;
        boolean homeExists = getStorage().getHome(target.getUniqueId(), home) != null;
        if (limit == -1)
            allow = true;
        else if (homeExists)
            allow = limit >= current;
        else
            allow = limit > current;

        if (allow) {
            if (getStorage().setHome(target.getUniqueId(), home, sender.getLocation()))
                if (homeExists) {
                    if (sender == target)
                        sender.sendMessage(prefixed("commands.sethome.relocate.other", sender, target.getName(), home));
                    else
                        sender.sendMessage(prefixed("commands.sethome.relocate", sender, home));
                } else {
                    if (sender == target)
                        sender.sendMessage(prefixed("commands.sethome.new.other", sender, target.getName(), home));
                    else
                        sender.sendMessage(prefixed("commands.sethome.new", sender, home));
                }
            else
                sender.sendMessage(error("commands.sethome.badLocation", sender));
        } else {
            sender.sendMessage(error("commands.sethome.reachedLimit", sender, limit, current));
        }
    }

    private void setDefaultHome(Player sender, String home) {
        if (getStorage().setDefaultHome(sender.getUniqueId(), home))
            sender.sendMessage(prefixed("commands.setdefaulthome", sender, home));
        else
            sender.sendMessage(error("commands.generic.home.failure", sender, home));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> ret = new ArrayList<>();
            if (sender instanceof Player) {
                for (String n : getStorage().getAllHomes(((Player) sender).getUniqueId()).keySet()) {
                    if (n.toLowerCase().startsWith(args[0].toLowerCase())) {
                        ret.add(n);
                    }
                }
            }

            if (hasOtherPermission(cmd, sender)) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        ret.add(p.getName());
                    }
                }
            }
            return ret;
        }

        if (args.length == 2 && hasOtherPermission(cmd, sender)) {
            UUID uuid = getStorage().getUniqueID(args[0]);
            if (uuid == null)
                return Collections.emptyList();

            List<String> ret = new ArrayList<>();
            for (String n : getStorage().getAllHomes(uuid).keySet()) {
                if (n.toLowerCase().startsWith(args[1].toLowerCase())) {
                    ret.add(n);
                }
            }
            return ret;
        }

        return Collections.emptyList();
    }

    private boolean hasOtherPermission(Command cmd, CommandSender sender) {
        return (cmd.getName().equalsIgnoreCase("home") && sender.hasPermission(OTHER_HOME_PERM))
                || (cmd.getName().equalsIgnoreCase("sethome") && sender.hasPermission(OTHER_SETHOME_PERM))
                || (cmd.getName().equalsIgnoreCase("delhome") && sender.hasPermission(OTHER_DELHOME_PERM));
    }
}
