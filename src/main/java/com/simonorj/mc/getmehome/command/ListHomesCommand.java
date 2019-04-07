package com.simonorj.mc.getmehome.command;

import com.simonorj.mc.getmehome.GetMeHome;
import com.simonorj.mc.getmehome.storage.HomeStorageAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;

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
            UUID uuid = getStorage().getUniqueID(args[0]);
            if (uuid == null) {
                sender.sendMessage(error("commands.generic.player.notFound", sender));
                return true;
            } else {
                get = plugin.getServer().getOfflinePlayer(uuid);
            }
        } else if (sender instanceof Player) {
            get = (Player) sender;
        } else {
            // When player is not found
            sender.sendMessage("Usage: /listhomes <player>");
            return true;
        }

        // Get home names owned by player
        Map<String, Location> homes = getStorage().getAllHomes(get.getUniqueId());
        String defaultHome = getStorage().getDefaultHomeName(get.getUniqueId());

        StringBuilder ret = new StringBuilder();

        if (get == sender)
            ret.append(prefixed("commands.listhomes.self", sender, homes.size(), plugin.getSetLimit((Player) get)));
        else if (get instanceof Player)
            ret.append(prefixed("commands.listhomes.other", sender, get.getName(), homes.size(), plugin.getSetLimit((Player) get)));
        else
            ret.append(prefixed("commands.listhomes.other.offline", sender, get.getName(), homes.size()));

        ret.append(plugin.getFocusColor());

        // List homes
        for (String name : homes.keySet()) {
            if (name.equals(defaultHome))
                ret.append(' ').append(ChatColor.BOLD).append(name).append(ChatColor.RESET).append(plugin.getFocusColor());
            else
                ret.append(' ').append(name);
        }

        sender.sendMessage(ret.toString());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission(OTHER_PERM))
            return null;

        return Collections.emptyList();
    }
}
