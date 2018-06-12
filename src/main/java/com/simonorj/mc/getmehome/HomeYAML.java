package com.simonorj.mc.getmehome;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

final class HomeYAML extends HomeStorage {
    private final File homeFile;
    private final FileConfiguration hc;
    private final HereIsYourHome plugin;
    private final boolean saveName;

    HomeYAML(HereIsYourHome pl) {
        plugin = pl;

        // Variable setup
        saveName = plugin.getConfig().getBoolean("storage.savename");

        // Storage setup
        homeFile = new File(plugin.getDataFolder(), "homes.yml");

        if (!homeFile.exists()) {
            plugin.saveResource("homes.yml", false);
        }

        hc = new YamlConfiguration();
        try {
            hc.load(homeFile);
            hc.save(homeFile);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    void onDisable() {
        try {
            hc.save(homeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("GetMeHome: Homes failed to save!");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    Location getHome(Player player, String name) {
        ConfigurationSection cs = hc.getConfigurationSection(player.getUniqueId().toString() + ".h." + name);
        if (cs == null)
            return null;

        Iterator<Double> c = cs.getDoubleList("c").iterator();
        Iterator<Float> y = cs.getFloatList("y").iterator();

        return new Location(
                plugin.getServer().getWorld(cs.getString("w")),
                c.next(),
                c.next(),
                c.next(),
                y.next(),
                y.next()
        );
    }

    @Override
    boolean setHome(Player player, String name, Location loc) {
        String uid = player.getUniqueId().toString();
        // Increment when adding another home
        ConfigurationSection cs = hc.getConfigurationSection(uid);
        if (cs == null) {
            cs = hc.createSection(uid);
        }

        // Update name
        if (saveName)
            cs.set("n", player.getName());

        cs = cs.getConfigurationSection("h");
        if (cs == null) {
            cs = hc.createSection(uid + ".h");
        }

        // Overwrite variable (and home name if it existed)
        cs = cs.createSection(name);
        cs.set("w", loc.getWorld().getName());
        List<Double> c = new ArrayList<>();
        c.add(loc.getX());
        c.add(loc.getY());
        c.add(loc.getZ());
        List<Float> y = new ArrayList<>();
        y.add(loc.getYaw());
        y.add(loc.getPitch());
        cs.set("c", c);
        cs.set("y", y);
        return true;
    }

    @Override
    int getNumberOfHomes(Player player) {
        // Size of configuration
        return hc.getConfigurationSection(player.getUniqueId() + ".h")
                .getKeys(false).size();
    }

    @Override
    boolean deleteHome(Player player, String name) {
        String path = player.getUniqueId() + ".h." + name;
        if (!hc.contains(path))
            return false;
        hc.set(path, null);
        return true;
    }

    @Override
    Map<String, Location> getAllHomes(Player player) {
        ConfigurationSection cs = hc.getConfigurationSection(player.getUniqueId() + ".h");
        HashMap<String, Location> ret = new HashMap<>();
        if (cs == null)
            return ret;
        for (String n : cs.getKeys(true)) {
            // Slightly modified version of getHome()
            ConfigurationSection csh = cs.getConfigurationSection(n);
            if (csh == null)
                continue;

            Iterator<Double> c = csh.getDoubleList("c").iterator();
            Iterator<Float> y = csh.getFloatList("y").iterator();

            ret.put(n, new Location(
                    plugin.getServer().getWorld(csh.getString("w")),
                    c.next(),
                    c.next(),
                    c.next(),
                    y.next(),
                    y.next()
            ));
        }
        return ret;
    }

    @Override
    Map<UUID, Map<String, Location>> getEntireList() {
        // TODO Auto-generated method stub
        return null;
    }
}
