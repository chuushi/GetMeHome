package com.simonorj.mc.getmehome.command;

import com.simonorj.mc.getmehome.GetMeHome;
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
    private final GetMeHome plugin;

    private final Map<Player, CooldownTimer> cooldownMap = new HashMap<>();
    private final Map<Player, WarmupTimer> warmupMap = new HashMap<>();

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

        if (cmd.getName().equalsIgnoreCase("home")) {
            @SuppressWarnings("SuspiciousMethodCalls") // sender is definitely an instance of Player
            WarmupTimer wt = warmupMap.get(sender);
            if (wt == null)
                home((Player) sender, target, home);
            else
                wt.incompleteCancel();
        } else if (cmd.getName().equalsIgnoreCase("sethome")) {
            setHome((Player) sender, target, home);
        } else if (cmd.getName().equalsIgnoreCase("setdefaulthome")) {
            setDefaultHome((Player) sender, home);
        } else if (cmd.getName().equalsIgnoreCase("delhome")) {
            deleteHome(sender, target, home);
        } else {
            return false;
        }

        return true;
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

    private void deleteHome(CommandSender sender, OfflinePlayer target, String home) {
        if (getStorage().deleteHome(target.getUniqueId(), home)) {
            String i18n = "commands.delhome" + (sender == target ? "" : ".other");
            sender.sendMessage(preparedMessage(i18n, sender, target, home));
        } else {
            sender.sendMessage(genericHomeFailureMessage(sender, target, home));
        }
    }

    private void home(Player sender, OfflinePlayer target, String home) {
        Location loc = getStorage().getHome(target.getUniqueId(), home);
        // No home
        if (loc == null) {
            sender.sendMessage(genericHomeFailureMessage(sender, target, home));
            return;
        }

        // Check if sender is still cooling down
        CooldownTimer ct = cooldownMap.get(sender);
        if (ct != null) {
            if (sender == target || !sender.hasPermission(DELAY_INSTANTOTHER_PERM)) {
                sender.sendMessage(prefixed("commands.home.cooldown", sender, ct.counter/20.0));
                return;
            }
            else {
                cooldownMap.remove(sender);
            }
        }

        // Welcome home!
        int delay = delayTeleport(sender, target);
        if (delay > 0) {
            boolean allowMove = sender.hasPermission(DELAY_ALLOWMOVE_PERM);
            sender.sendMessage(prefixed("commands.home.warmup" + (allowMove ? "" : ".still"), sender, delay/20.0));
            WarmupTimer wt = new WarmupTimer(sender, target, home, loc, delay, !allowMove);
            wt.runTaskTimerAsynchronously(plugin, 1L, 1L);
            warmupMap.put(sender, wt);
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

    // I may want to move this and related stuff into another class
    private void setupCooldown(Player p) {
        int time = plugin.getCooldown().calcFor(p).value;
        if (time == 0)
            return;

        CooldownTimer tmr = new CooldownTimer(p, time);
        tmr.runTaskTimerAsynchronously(plugin, 1L, 1L);
        cooldownMap.put(p, tmr);
    }

    private class CooldownTimer extends BukkitRunnable {
        Player player;
        int counter;

        private CooldownTimer(Player p, int counter) {
            this.player = p;
            this.counter = counter;
        }

        @Override
        public void run() {
            if (--counter == 0) {
                cooldownMap.remove(player);
                this.cancel();
            }
        }
    }

    private void teleportHome(Player sender, OfflinePlayer target, String home, Location loc) {
        boolean farAway;
        if (sender.getWorld() == loc.getWorld()) {
            double dist = loc.distanceSquared(sender.getLocation());
            farAway = dist > plugin.getWelcomeHomeRadiusSquared();
        } else {
            farAway = true;
        }

        if (sender.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND)) {
            setupCooldown(sender);
            if (farAway) {
                String i18n = "commands.home"
                        + (sender == target ? "" : ".other")
                        + ".success";
                sender.sendMessage(preparedMessage(i18n, sender, target, home));
            }
        } else {
            sender.sendMessage(error("commands.home.unable", sender, home));
        }
    }

    private class WarmupTimer extends BukkitRunnable {
        private final Player sender;
        private final OfflinePlayer homeOwner;
        private final String home;
        private final Location location;
        private final boolean checkMovement;

        private int counter;
        private Location polledLocation;

        private WarmupTimer(Player sender, OfflinePlayer homeOwner, String home, Location location, int counter, boolean checkMovement) {
            this.sender = sender;
            this.homeOwner = homeOwner;
            this.home = home;
            this.location = location;
            this.counter = counter;
            this.checkMovement = checkMovement;

            this.polledLocation = sender.getLocation();
        }

        @Override
        public void run() {
            if (checkMovement && counter % 10 == 0) {
                Location loc = sender.getLocation();

                if (polledLocation.distanceSquared(loc) >= 0.4) {
                    incompleteCancel();
                    return;
                } else {
                    polledLocation = loc;
                }
            }

            if (--counter == 0) {
                this.cancel();
                warmupMap.remove(sender);

                // Move it back over to main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        teleportHome(sender, homeOwner, home, location);
                    }
                }.runTask(plugin);
            }
        }

        private void incompleteCancel() {
            this.cancel();
            warmupMap.remove(sender);
            sender.sendMessage(prefixed("commands.home.warmup.cancel", sender));
        }
    }

    private void setHome(Player sender, OfflinePlayer target, String home) {
        YamlPermValue.WorldValue wv = target instanceof Player ? plugin.getLimit().calcFor(sender) : null;

        int limit = wv == null ? -1 : wv.value;
        int current;
        int exempt = 0;
        if (limit == -1) {
            current = getStorage().getNumberOfHomes(target.getUniqueId(), wv == null ? null : wv.worlds);
        } else {
            current = 0;

            for (Map.Entry<String, Integer> hpw : getStorage().getNumberOfHomesPerWorld(target.getUniqueId(), wv.worlds).entrySet()) {
                if (wv.deducts != null) {
                    for (YamlPermValue.WorldValue wvd : wv.deducts) {
                        if (wvd.value != 0
                                && wvd.worlds.contains(hpw.getKey())) {
                            if (wvd.value != -1)
                                wvd.value--;
                            exempt++;
                            break;
                        } else {
                            current++;
                        }
                    }
                }
            }
        }

        boolean allow;
        boolean homeExists = getStorage().getHome(target.getUniqueId(), home) != null;

        if (limit == -1)
            allow = true;
        else {
            if (homeExists)
                allow = limit >= current;
            else
                allow = limit > current;
        }

        if (allow) {
            if (getStorage().setHome(target.getUniqueId(), home, sender.getLocation())) {
                String i18n = "commands.sethome."
                        + (homeExists ? "relocate" : "new")
                        + (sender == target ? "" : ".other");
                sender.sendMessage(preparedMessage(i18n, sender, target, home));
            } else {
                sender.sendMessage(error("commands.sethome.badLocation", sender));
            }
        } else {
            Object now = exempt == 0 ? current : current + "(+" + exempt + ")";
            sender.sendMessage(error("commands.sethome.reachedLimit", sender, limit, current));
        }
    }

    private void setDefaultHome(Player sender, String home) {
        if (getStorage().setDefaultHome(sender.getUniqueId(), home))
            sender.sendMessage(prefixed("commands.setdefaulthome", sender, home));
        else
            sender.sendMessage(error("commands.generic.home.failure", sender, home));
    }

    private String preparedMessage(String i18n, CommandSender sender, OfflinePlayer target, String home) {
        if (sender == target)
            return prefixed(i18n, sender, home);
        else
            return prefixed(i18n, sender, target.getName(), home);
    }

    private String genericHomeFailureMessage(CommandSender sender, OfflinePlayer target, String home) {
        if (sender == target)
            return error("commands.generic.home.failure", sender, home);
        else
            return error("commands.generic.home.other.failure", sender, target.getName(), home);
    }

    private boolean hasOtherPermission(Command cmd, CommandSender sender) {
        return (cmd.getName().equalsIgnoreCase("home") && sender.hasPermission(OTHER_HOME_PERM))
                || (cmd.getName().equalsIgnoreCase("sethome") && sender.hasPermission(OTHER_SETHOME_PERM))
                || (cmd.getName().equalsIgnoreCase("delhome") && sender.hasPermission(OTHER_DELHOME_PERM));
    }
}
