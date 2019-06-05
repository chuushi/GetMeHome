package com.simonorj.mc.getmehome.command;

import com.simonorj.mc.getmehome.GetMeHome;
import com.simonorj.mc.getmehome.DelayTimer;
import com.simonorj.mc.getmehome.I18n;
import com.simonorj.mc.getmehome.config.YamlPermValue;
import com.simonorj.mc.getmehome.storage.HomeStorageAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static com.simonorj.mc.getmehome.MessageTool.error;
import static com.simonorj.mc.getmehome.MessageTool.prefixed;

public class HomeCommands implements TabExecutor {
    private static final String OTHER_HOME_PERM = "getmehome.command.home.other";
    private static final String OTHER_SETHOME_PERM = "getmehome.command.sethome.other";
    private static final String OTHER_DELHOME_PERM = "getmehome.command.delhome.other";
    private static final String DELAY_INSTANTOTHER_PERM = "getmehome.delay.instantother";
    private static final String DELAY_ALLOWMOVE_PERM = "getmehome.delay.allowmove";

    private final DelayTimer delayTimer;
    private final GetMeHome plugin;

    public HomeCommands(GetMeHome plugin) {
        this.plugin = plugin;
        this.delayTimer = new DelayTimer(plugin);
    }

    private HomeStorageAPI getStorage() {
        return plugin.getStorage();
    }

    @Override
    public boolean onCommand(CommandSender sender, final Command cmd, String label, final String[] args) {
        OfflinePlayer target;
        boolean otherHome;

        // parse target player
        if ((args.length >= 2) && hasOtherPermission(cmd, sender)) {
            target = plugin.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(error(I18n.CMD_GENERIC_PLAYER_NOT_FOUND, sender));
                return true;
            }
            otherHome = true;
        } else if (sender instanceof Player) {
            target = (Player) sender;
            otherHome = false;
        } else {
            consoleCommand(sender, cmd.getName());
            return true;
        }

        // parse home name
        String home;
        if (args.length >= 2 && otherHome)
            home = args[1];
        else if (args.length != 0)
            home = args[0];
        else
            home = getStorage().getDefaultHomeName(target.getUniqueId());

        // Run command
        switch (cmd.getName().toLowerCase()) {
            case "home":
                home((Player) sender, target, home);
                break;
            case "sethome":
                setHome((Player) sender, target, home);
                break;
            case "setdefaulthome":
                setDefaultHome((Player) sender, home);
                break;
            case "delhome":
                deleteHome(sender, target, home);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> ret = new ArrayList<>();
            if (sender instanceof Player) {
                addHomeNames(ret, ((Player) sender).getUniqueId(), args[0]);
            }

            if (hasOtherPermission(cmd, sender)) {
                addPlayerNames(ret, args[0]);
            }
            return ret;
        }

        if (args.length == 2 && hasOtherPermission(cmd, sender)) {
            UUID uuid = getStorage().getUniqueID(args[0]);
            if (uuid == null)
                return Collections.emptyList();

            List<String> ret = new ArrayList<>();
            addHomeNames(ret, uuid, args[1]);
            return ret;
        }

        return Collections.emptyList();
    }

    private void addPlayerNames(List<String> list, String start) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase().startsWith(start.toLowerCase())) {
                list.add(p.getName());
            }
        }
    }

    private void addHomeNames(List<String> list, UUID uuid, String start) {
        for (String n : getStorage().getAllHomes(uuid).keySet()) {
            if (n.toLowerCase().startsWith(start.toLowerCase())) {
                list.add(n);
            }
        }
    }

    private void consoleCommand(CommandSender sender, String command) {
        if (command.equalsIgnoreCase("delhome"))
            sender.sendMessage("Usage: /delhome <player> <home name>");
        else
            sender.sendMessage("You must be a player");
    }

    private void deleteHome(CommandSender sender, OfflinePlayer target, String home) {
        if (getStorage().deleteHome(target.getUniqueId(), home)) {
            sender.sendMessage(preparedMessage(
                    sender == target
                            ? I18n.CMD_DELHOME
                            : I18n.CMD_DELHOME_OTHER
                    , sender, target, home));
        } else {
            sender.sendMessage(genericHomeFailureMessage(sender, target, home));
        }
    }

    private void home(Player sender, OfflinePlayer target, String home) {
        // If in warmup timer
        if (delayTimer.cancelWarmup(sender))
            return;

        Location loc = getStorage().getHome(target.getUniqueId(), home);
        // No home
        if (loc == null) {
            sender.sendMessage(genericHomeFailureMessage(sender, target, home));
            return;
        }

        // Check if sender is still cooling down
        int coolTick = delayTimer.getCooldown(sender);
        if (coolTick != 0) {
            if (sender == target || !sender.hasPermission(DELAY_INSTANTOTHER_PERM)) {
                sender.sendMessage(prefixed(I18n.CMD_HOME_COOLDOWN, sender, coolTick/20.0));
                return;
            }
            else {
                delayTimer.cancelCooldown(sender);
            }
        }

        // Welcome home!
        int delay = delayTeleport(sender, target);
        if (delay > 0) {
            boolean allowMove = sender.hasPermission(DELAY_ALLOWMOVE_PERM);
            sender.sendMessage(prefixed(
                    allowMove
                            ? I18n.CMD_HOME_WARMUP
                            : I18n.CMD_HOME_WARMUP_STILL
                    , sender, delay/20.0));

            BukkitRunnable onTime = new BukkitRunnable() {
                @Override
                public void run() {
                    teleportHome(sender, target, home, loc);
                }
            };

            delayTimer.newWarmup(sender, delay, !allowMove, onTime); // TODO: Put teleportHome logic here probably by using Runnable() lambda function
        } else {
            teleportHome(sender, target, home, loc);
        }
   }

    private int delayTeleport(Player sender, OfflinePlayer homeOwner) {
        if (sender == homeOwner || !sender.hasPermission(DELAY_INSTANTOTHER_PERM)) {
            YamlPermValue.WorldValue wv = plugin.getWarmup().calcFor(sender);

            return wv.value;
        }

        return 0;
    }

    private void teleportHome(Player sender, OfflinePlayer target, String home, Location loc) {
        boolean farAway;

        // Welcome Home Message Calculator
        if (sender.getWorld() == loc.getWorld()) {
            double dist = loc.distanceSquared(sender.getLocation());
            farAway = dist > plugin.getWelcomeHomeRadiusSquared();
        } else {
            farAway = true;
        }

        // Teleporter
        if (sender.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND)) {
            if (farAway) {
                sender.sendMessage(preparedMessage(
                        sender == target
                                ? I18n.CMD_HOME_SUCCESS
                                : I18n.CMD_HOME_OTHER_SUCCESS
                        , sender, target, home));
            }

            int timer = plugin.getCooldown().calcFor(sender).value;
            if (timer >= 0)
                delayTimer.newCooldown(sender, timer);

        } else {
            sender.sendMessage(error(I18n.CMD_HOME_UNABLE, sender, home));
        }
    }


    private void setHome(Player sender, OfflinePlayer target, String home) {
        YamlPermValue.WorldValue wv = target instanceof Player ? plugin.getLimit().calcFor(sender) : null;

        int limit = wv == null ? -1 : wv.value;
        int current = getStorage().getNumberOfHomes(target.getUniqueId(), wv == null ? null : wv.worlds);
        int exempt = 0;

        Location homeLoc = getStorage().getHome(target.getUniqueId(), home);
        boolean homeExists = homeLoc != null;
        boolean notExempt = true;
        String homeWorld = homeExists ? homeLoc.getWorld().getName().toLowerCase() : null;
        boolean allow;

        // Calculate count deductions (just have faith in this logic; it looks complicated but it works perfectly)
        if (limit != -1 && wv.deducts != null) {
            // For home count in each world
            for (Map.Entry<String, Integer> hpw : getStorage().getNumberOfHomesPerWorld(target.getUniqueId(), wv.worlds).entrySet()) {
                // For each deduction origin
                for (YamlPermValue.WorldValue wvd : wv.deducts) {
                    if (wvd.value != 0 && wvd.worlds.contains(hpw.getKey())) {
                        if (wvd.value != -1)
                            wvd.value--;
                        exempt++;

                        if (wvd.worlds.contains(homeWorld))
                            notExempt = false;

                        break;
                    }
                }
            }
            current -= exempt;
        }

        // Check if to allow home setting
        if (limit == -1)
            allow = true;
        else if (homeExists && notExempt)
            allow = limit >= current;
        else
            allow = limit > current;

        if (allow) {
            if (getStorage().setHome(target.getUniqueId(), home, sender.getLocation())) {
                I18n i18n;

                if (homeExists) {
                    if (sender == target)
                        i18n = I18n.CMD_SETHOME_RELOCATE;
                    else
                        i18n = I18n.CMD_SETHOME_RELOCATE_OTHER;
                } else {
                    if (sender == target)
                        i18n = I18n.CMD_SETHOME_NEW;
                    else
                        i18n = I18n.CMD_SETHOME_NEW_OTHER;
                }

                sender.sendMessage(preparedMessage(i18n, sender, target, home));
            } else {
                sender.sendMessage(error(I18n.CMD_SETHOME_BAD_LOCATION, sender));
            }
        } else {
            Object now = exempt == 0
                    ? current
                    : current + "(+" + exempt + ")";
            sender.sendMessage(error(I18n.CMD_SETHOME_REACHED_LIMIT, sender, limit, now));
        }
    }

    private void setDefaultHome(Player sender, String home) {
        if (getStorage().setDefaultHome(sender.getUniqueId(), home))
            sender.sendMessage(prefixed(I18n.CMD_SETDEFAULTHOME, sender, home));
        else
            sender.sendMessage(error(I18n.CMD_GENERIC_HOME_FAILURE, sender, home));
    }

    private String preparedMessage(I18n i18n, CommandSender sender, OfflinePlayer target, String home) {
        if (sender == target)
            return prefixed(i18n, sender, home);
        else
            return prefixed(i18n, sender, target.getName(), home);
    }

    private String genericHomeFailureMessage(CommandSender sender, OfflinePlayer target, String home) {
        if (sender == target)
            return error(I18n.CMD_GENERIC_HOME_FAILURE, sender, home);
        else
            return error(I18n.CMD_GENERIC_HOME_OTHER_FAILURE, sender, target.getName(), home);
    }

    private boolean hasOtherPermission(Command cmd, CommandSender sender) {
        return (cmd.getName().equalsIgnoreCase("home") && sender.hasPermission(OTHER_HOME_PERM))
                || (cmd.getName().equalsIgnoreCase("sethome") && sender.hasPermission(OTHER_SETHOME_PERM))
                || (cmd.getName().equalsIgnoreCase("delhome") && sender.hasPermission(OTHER_DELHOME_PERM));
    }
}
