package com.simonorj.mc.getmehome.command;

import com.simonorj.mc.getmehome.GetMeHome;
import com.simonorj.mc.getmehome.config.YamlPermValue;
import com.simonorj.mc.getmehome.storage.HomeStorageAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.simonorj.mc.getmehome.MessageTool.*;

public class ListHomesCommand implements TabExecutor {
    private static final String OTHER_PERM = "getmehome.command.listhomes.other";
    private static final String GLOBAL_FLAG = "-global";
    private static final String GLOBAL_SHORT_FLAG = "-g";
    private GetMeHome plugin;

    public ListHomesCommand(GetMeHome plugin) {
        this.plugin = plugin;
    }

    private HomeStorageAPI getStorage() {
        return plugin.getStorage();
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, final String[] args) {
        // Get player in question
        OfflinePlayer get;
        boolean global;
        String getName;

        if (args.length != 0) {
            if (args[0].equalsIgnoreCase(GLOBAL_FLAG) || args[0].equalsIgnoreCase(GLOBAL_SHORT_FLAG)) {
                global = true;
                getName = args.length > 1 ? args[1] : null;
            } else {
                getName = args[0];
                global = false;
            }
        } else {
            getName = null;
            global = false;
        }

        if (sender.hasPermission(OTHER_PERM) && getName != null) {
            get = plugin.getPlayer(getName);
            if (get == null) {
                sender.sendMessage(error("commands.generic.player.notFound", sender));
                return true;
            }
        } else if (sender instanceof Player) {
            get = (Player) sender;
        } else {
            // When player is not found
            sender.sendMessage("Usage: /listhomes [-global] <player>");
            return true;
        }

        listHomes(sender, get, global);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> ret = new ArrayList<>();
            String lower = args[0].toLowerCase();

            if (sender.hasPermission(OTHER_PERM)) {
                plugin.getServer().getOnlinePlayers().forEach(p -> {
                    if (p.getName().toLowerCase().startsWith(lower)) {
                        ret.add(p.getName());
                    }
                });
            }

            if (GLOBAL_FLAG.startsWith(lower))
                ret.add(GLOBAL_FLAG);

            return ret;
        } else if (args.length == 2 && sender.hasPermission(OTHER_PERM) && args[0].equalsIgnoreCase(GLOBAL_FLAG)) {
            return null;
        }

        return Collections.emptyList();
    }

    private void listHomes(CommandSender sender, OfflinePlayer target, boolean global) {
        YamlPermValue.WorldValue wv = (target instanceof Player) ? plugin.getLimit().calcFor((Player) target) : null;

        // Get home names owned by player
        Map<String, Location> homes = getStorage().getAllHomes(target.getUniqueId(), global || wv == null ? null : wv.worlds);
        String defaultHome = getStorage().getDefaultHomeName(target.getUniqueId());

        Iterator<Map.Entry<String, Location>> i = homes.entrySet().iterator();
        AtomicInteger effective = new AtomicInteger(0);
        StringBuilder list;

        if (i.hasNext()) {

            ChatColor f = plugin.getFocusColor();
            ChatColor c = plugin.getContentColor();
            list = new StringBuilder(homeName(i.next(), wv, effective, defaultHome));

            while (i.hasNext()) {
                list.append(c).append(", ").append(f).append(homeName(i.next(), wv, effective, defaultHome));
            }
        } else {
            list = new StringBuilder(ChatColor.ITALIC.toString()).append(raw("commands.listhomes.none", sender));
        }

        // TODO: Append "Deduct" value to current home count
        Object total = wv == null ? null : wv.worlds != null && global ? "?" : wv.value;
        Object count = effective.get() == 0 || effective.get() == homes.size() ? homes.size() : effective.get() + "(+" + (homes.size() - effective.get()) + ")";

        if (target == sender)
            sender.sendMessage(prefixed("commands.listhomes.self", sender, count, total, list.toString()));
        else if (target instanceof Player)
            sender.sendMessage(prefixed("commands.listhomes.other", sender, target.getName(), count, total, list.toString()));
        else
            sender.sendMessage(prefixed("commands.listhomes.other.offline", sender, target.getName(), count, list.toString()));
    }

    private String homeName(Map.Entry<String, Location> d, YamlPermValue.WorldValue wv, AtomicInteger effective, String defaultHome) {
        boolean deductable = false;
        if (wv != null && wv.deducts != null) {
            String world = d.getValue().getWorld().getName().toLowerCase();

            for (YamlPermValue.WorldValue wvd : wv.deducts) {
                if (wvd.value != 0) {
                    if (wvd.worlds.contains(world)) {
                        deductable = true;
                        if (wvd.value != -1)
                            wvd.value--;
                        break;
                    } else {
                        effective.incrementAndGet();
                    }
                }
            }
        }

        StringBuilder ret = new StringBuilder();
        if (d.getKey().equals(defaultHome))
            ret.append(ChatColor.BOLD);
        if (deductable)
            ret.append(ChatColor.ITALIC);
        ret.append(d.getKey()).append(ChatColor.RESET);
        return ret.toString();
    }
}
