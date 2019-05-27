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
import java.util.function.Function;

import static com.simonorj.mc.getmehome.MessageTool.*;

public class ListHomesCommand implements TabExecutor {
    private static final String OTHER_PERM = "getmehome.command.listhomes.other";
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
        if (sender.hasPermission(OTHER_PERM) && args.length != 0) {
            get = plugin.getPlayer(args[0]);
            if (get == null) {
                sender.sendMessage(error("commands.generic.player.notFound", sender));
                return true;
            }
        } else if (sender instanceof Player) {
            get = (Player) sender;
        } else {
            // When player is not found
            sender.sendMessage("Usage: /listhomes <player>");
            return true;
        }

        listHomes(sender, get);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission(OTHER_PERM))
            return null;

        return Collections.emptyList();
    }

    private void listHomes(CommandSender sender, OfflinePlayer target) {
        YamlPermValue.WorldValue wv = (target instanceof Player) ? plugin.getLimit().calcFor((Player) target) : null;

        // Get home names owned by player
        Map<String, Location> homes = getStorage().getAllHomes(target.getUniqueId(), wv == null ? null : wv.worlds);
        String defaultHome = getStorage().getDefaultHomeName(target.getUniqueId());

        Iterator<String> i = homes.keySet().iterator();
        StringBuilder list;

        if (i.hasNext()) {
            Function<String, String> parse = d -> {
                if (d.equals(defaultHome))
                    return ChatColor.BOLD + d + ChatColor.RESET;
                else
                    return d;
            };

            ChatColor f = plugin.getFocusColor();
            ChatColor c = plugin.getContentColor();
            list = new StringBuilder(parse.apply(i.next()));

            while (i.hasNext()) {
                list.append(c).append(", ").append(f).append(parse.apply(i.next()));
            }
        } else {
            list = new StringBuilder(ChatColor.ITALIC.toString()).append(raw("commands.listhomes.none", sender));
        }

        // TODO: World-based homes

        if (target == sender)
            sender.sendMessage(prefixed("commands.listhomes.self", sender, homes.size(), wv.value, list.toString()));
        else if (target instanceof Player)
            sender.sendMessage(prefixed("commands.listhomes.other", sender, target.getName(), homes.size(), wv.value, list.toString()));
        else
            sender.sendMessage(prefixed("commands.listhomes.other.offline", sender, target.getName(), homes.size(), list.toString()));
    }
}
