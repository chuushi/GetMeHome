package com.simonorj.mc.getmehome.command;

import com.google.common.collect.ImmutableList;
import com.simonorj.mc.getmehome.GetMeHome;
import com.simonorj.mc.getmehome.I18n;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

import static com.simonorj.mc.getmehome.MessageTool.*;

public class MetaCommand implements TabExecutor {
    private static final String RELOAD_PERM = "getmehome.reload";
    private final GetMeHome plugin = GetMeHome.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 0 && sender.hasPermission(RELOAD_PERM)) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                plugin.loadConfig();
                plugin.loadStorage();

                sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully.");
                return true;
            }

            if (args[0].equalsIgnoreCase("clearcache")) {
                plugin.getStorage().clearCache();
                sender.sendMessage(ChatColor.GREEN + "Cache cleared.");
                return true;
            }
        }

        sender.sendMessage(prefixed(I18n.CMD_META_HEADING, sender, plugin.getDescription().getVersion(), plugin.getDescription().getAuthors().get(0)));
        sender.sendMessage(prefixed(I18n.CMD_META_TRANSLATED, sender, raw(I18n.LANGUAGE_TRANSLATED_BY, sender)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission(RELOAD_PERM) && args.length == 1) {
            ImmutableList.Builder<String> ret = ImmutableList.builder();

            String low = args[0].toLowerCase();
            if ("reload".startsWith(low))
                ret.add("reload");

            if ("clearcache".startsWith(low))
                ret.add("clearcache");

            return ret.build();
        }
        return ImmutableList.of();
    }
}
