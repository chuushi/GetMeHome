package com.simonorj.mc.getmehome.command;

import com.google.common.collect.ImmutableList;
import com.simonorj.mc.getmehome.storage.StorageYAML;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class MetaCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            messageTo(sender, "GetMeHome Version: " + getDescription().getVersion());
            messageTo(sender, "by " + getDescription().getAuthors().get(0));
            // Display list of commands
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (storage instanceof StorageYAML) {
                if (args.length != 2 || !(args[1].equalsIgnoreCase("yes") || args[1].equalsIgnoreCase("no"))) {
                    BaseComponent prompt = new TextComponent("GetMeHome: Overwrite homes.yml? ");

                    BaseComponent yes = new TranslatableComponent("gui.yes");
                    yes.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + label + " " + args[0] +" yes"));
                    yes.setColor(ChatColor.AQUA);

                    BaseComponent no = new TranslatableComponent("gui.no");
                    no.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + label + " " + args[0] +" no"));
                    no.setColor(ChatColor.AQUA);

                    prompt.addExtra(yes);
                    prompt.addExtra(" ");
                    prompt.addExtra(no);

                    messageTo(sender, prompt);
                    return true;
                }
            }
            reloadConfig();
            loadConfiguration();
            if (!(storage instanceof StorageYAML && args.length == 2 && args[1].equalsIgnoreCase("no")))
                storage.save();
            loadStorage();

            messageTo(sender, "GetMeHome: " + ChatColor.GREEN + "Configuration reloaded successfully.");

            return true;
        }

        if (args[0].equalsIgnoreCase("clearcache")) {
            storage.clearCache();
            messageTo(sender, "GetMeHome: Cache cleared.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("getmehome.reload"))
            return ImmutableList.of();
        if (args.length <= 1 && "reload".startsWith(args[0].toLowerCase()))
            return ImmutableList.of("reload");
        return ImmutableList.of();
    }
}
