package com.simonorj.mc.getmehome;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GetMeHome extends JavaPlugin {
    private HomeStorage storage;
    private List<HomePermissionLimit> homePermissionLimit;
    private int defaultLimit;
    private ChatColor color;
    private boolean italic, bold, underline;

    private final class HomePermissionLimit {
        private final String permission;
        private final int limit;
        HomePermissionLimit(String permission, int limit) {
            this.permission = permission;
            this.limit = limit;
        }
        String getPermission() {
            return permission;
        }

        int getLimit() {
            return limit;
        }

    }

    @Override
    public void onEnable() {
        // Get config
        saveDefaultConfig();
        loadConfiguration();
        loadStorage();

        HomeCommand hc = new HomeCommand(this);
        getCommand("home").setExecutor(hc);
        getCommand("sethome").setExecutor(hc);
        getCommand("setdefaulthome").setExecutor(hc);
        getCommand("delhome").setExecutor(hc);
        getCommand("home").setTabCompleter(hc);
        getCommand("sethome").setTabCompleter(hc);
        getCommand("setdefaulthome").setTabCompleter(hc);
        getCommand("delhome").setTabCompleter(hc);

        getCommand("getmehome").setTabCompleter(new GetMeHomeTab());
        getCommand("listhomes").setExecutor(new ListHomesCommand(this));

        getServer().getPluginManager().registerEvents(new SavingDetector(), this);
    }

    @Override
    public void onDisable() {
        if (storage != null)
            storage.save();
    }

    private void loadConfiguration() {
        homePermissionLimit = new ArrayList<>();

        defaultLimit = getConfig().getInt("limit.default", 1);
        ConfigurationSection csl = getConfig().getConfigurationSection("limit");

        if (csl == null) {
            getLogger().warning("Configuration invalid or missing: limit");
            return;
        }


        for (String s : csl.getKeys(true)) {
            // Skip default and non-number node
            if (s.equals("default") || !csl.isInt(s))
                continue;

            // put it in
            homePermissionLimit.add(new HomePermissionLimit(s, csl.getInt(s)));
        }

        color = ChatColor.getByChar(getConfig().getString("formatting.color", "e").charAt(0));
        italic =    getConfig().getBoolean("formatting.italic");
        bold =      getConfig().getBoolean("formatting.bold");
        underline = getConfig().getBoolean("formatting.underline");
    }

    private void loadStorage() {
        if (!getConfig().contains("storage.type"))
            getLogger().warning(getConfig().contains("storage") ? "storage is empty" : "storage.type is not set");
        else {
            ConfigurationSection cs = getConfig().getConfigurationSection("storage");
            String type = cs.getString("type");

            if (type.equalsIgnoreCase("mysql")) {
                if (cs.contains("database")) {
                    getLogger().warning("storage.database is empty. Using YAML storage method.");
                } else {
                    storage = new StorageSQL(this);
                    return;
                }
            } else if (!type.equalsIgnoreCase("yaml")) {
                    getLogger().warning("storage.type contains illegal type. Using YAML storage method.");
            }

            storage = new StorageYAML(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("getmehome")) {
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
        return false;
    }

    HomeStorage getStorage() {
        return storage;
    }

    int getSetLimit(Player p) {
        // Override if has permission node
        for (HomePermissionLimit l : homePermissionLimit) {
            if (p.hasPermission(l.getPermission())) {
                return l.getLimit();
            }
        }
        return defaultLimit;
    }

    void messageTo(CommandSender sender, BaseComponent msg) {
        msg.setColor(color);
        msg.setItalic(italic);
        msg.setBold(bold);
        msg.setUnderlined(underline);

        if (sender instanceof Player) ((Player)sender).spigot().sendMessage(ChatMessageType.SYSTEM, msg);
        else sender.sendMessage(msg.toLegacyText());
    }

    void messageTo(CommandSender sender, String msg) {
        String send = color.toString();
        if (italic) send += ChatColor.ITALIC;
        if (bold) send += ChatColor.BOLD;
        if (underline) send += ChatColor.UNDERLINE;

        send += msg;

        sender.sendMessage(send);
    }

    public class GetMeHomeTab implements TabCompleter {
        private final List<String> list;

        private GetMeHomeTab() {
             list = new ArrayList<>();
             list.add("reload");
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            if (sender.hasPermission("getmehome.reload"))
                return list;
            else
                return Collections.emptyList();
        }
    }

    public final class SavingDetector implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void allOut(PlayerQuitEvent e) {
            if (getServer().getOnlinePlayers().size() <= 1) {
                storage.save();
                storage.clearCache();
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void worldSave(WorldSaveEvent e) {
            // Save home data
            storage.save();
        }
    }
}
