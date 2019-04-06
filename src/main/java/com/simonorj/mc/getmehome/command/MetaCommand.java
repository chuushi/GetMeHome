package com.simonorj.mc.getmehome.command;

import com.google.common.collect.ImmutableList;
import com.simonorj.mc.getmehome.GetMeHome;
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

        sender.sendMessage(prefixed("commands.meta.heading", sender, plugin.getDescription().getVersion(), plugin.getDescription().getAuthors().get(0)));
        sender.sendMessage(prefixed("commands.meta.translated", sender, raw("language.translatedBy", sender)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission(RELOAD_PERM) && args.length == 1)
            return ImmutableList.of("reload", "clearcache");
        return ImmutableList.of();
    }
}
