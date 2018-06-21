package com.simonorj.mc.getmehome;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.util.*;

public class ListHomesCommand implements TabExecutor {
    private GetMeHome plugin;

    ListHomesCommand(GetMeHome plugin) {
        this.plugin = plugin;
    }

    private HomeStorage getStorage() {
        return plugin.getStorage();
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command cmd, String label, final String[] args) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                // Get player in question
                OfflinePlayer get;
                if (sender.hasPermission("getmehome.otherhome") && args.length != 0)
                    get = plugin.getServer().getOfflinePlayer(getStorage().getUniqueID(args[0]));
                else if (sender instanceof Player)
                    get = (Player) sender;
                else
                    get = null;

                if (get == null) {
                    // When player is not found
                    BaseComponent ret = new TranslatableComponent("commands.generic.player.notFound");
                    ret.setColor(ChatColor.RED);
                    if (sender instanceof Player)
                        ((Player) sender).spigot().sendMessage(ChatMessageType.SYSTEM, ret);
                    else
                        sender.sendMessage("That player cannot be found");
                    return;
                }

                // Get home names owned by player
                Map<String, Location> homes = getStorage().getAllHomes(get);
                String defaultHome = getStorage().getDefaultHomeName(get);

                // Localization
                String[] temp = {"en", "US"};
                if (sender instanceof Player)
                    temp = ((Player) sender).spigot().getLocale().split("_");
                ResourceBundle localize = ResourceBundle.getBundle("GetMeHome", new Locale(temp[0], temp[1]));

                BaseComponent ret;
                if (get == sender)
                    ret = new TextComponent(String.format(localize.getString("commands.listhomes.self"),
                            homes.size(), plugin.getSetLimit((Player) get)));
                else if (get instanceof Player)
                    ret = new TextComponent(String.format(localize.getString("commands.listhomes.other"),
                            get.getName(), homes.size(), plugin.getSetLimit((Player) get)));
                else
                    ret = new TextComponent(String.format(localize.getString("commands.listhomes.other.offline"),
                            get.getName(), homes.size()));


                // List homes
                for (String name : homes.keySet()) {
                    Location home = homes.get(name);
                    BaseComponent t = new TextComponent("[" + name + "]");
                    t.setColor(ChatColor.AQUA);
                    if (name.equals(defaultHome))
                        t.setBold(true);

                    if (get == sender)
                        t.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/home " + name));
                    else
                        t.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                                "/tp " + home.getX() + " " + home.getY() + " " + home.getZ()));
                    if (sender.hasPermission("getmehome.otherhome"))
                        t.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Coordinates: " + home.getBlockX() + " " + home.getBlockY() + " " + home.getBlockZ())
                                        .create()));

                    ret.addExtra(" ");
                    ret.addExtra(t);
                }

                plugin.messageTo(sender, ret);
            }
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("getmehome.otherhome"))
            return null;
        else
            return Collections.emptyList();
    }
}
