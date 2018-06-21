package com.simonorj.mc.getmehome;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

final class StorageYAML extends HomeStorage {
    private final File homeFile;
    private final FileConfiguration hc;
    private final GetMeHome plugin;
    private final boolean saveName;

    /*
     * Home structure:
     * names:
     *   NAME: UUID
     *
     * UUID:
     *   n: PLAYER NAME
     *   d: DEFAULT HOME NAME
     *   h:
     *     HOMENAME:
     *       c:
     *       - X
     *       - Y
     *       - Z
     *       y:
     *       - YAW
     *       - PITCH
     */

    StorageYAML(GetMeHome pl) {
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
    void save() {
        try {
            hc.save(homeFile);
        } catch (IOException e) {
            plugin.getLogger().warning("GetMeHome: Homes failed to save!");
            e.printStackTrace();
        }
    }

    @Override
    UUID getUniqueID(String player) {
        String uuid = hc.getString("names." + player.toLowerCase());
        if (uuid == null)
            return null;
        return UUID.fromString(uuid);
    }

    @Override
    Location getHome(OfflinePlayer player, String name) {
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
    String getDefaultHomeName(OfflinePlayer player) {
        String ret = hc.getString(player.getUniqueId().toString() + ".d");
        if (ret == null)
            return "default";
        return ret;
    }

    @Override
    boolean setDefaultHome(OfflinePlayer player, String name) {
        if (hc.getConfigurationSection(player.getUniqueId().toString() + ".h." + name) == null)
            return false;
        hc.set(player.getUniqueId().toString() + ".d", name);
        return true;
    }

    @Override
    boolean setHome(OfflinePlayer player, String name, Location loc) {
        String uid = player.getUniqueId().toString();
        // Increment when adding another home
        ConfigurationSection cs = hc.getConfigurationSection(uid);
        if (cs == null) {
            cs = hc.createSection(uid);
        }

        // Update name
        hc.set("names." + player.getName().toLowerCase(), player.getUniqueId().toString());
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
    int getNumberOfHomes(OfflinePlayer player) {
        // Size of configuration
        ConfigurationSection cs = hc.getConfigurationSection(player.getUniqueId() + ".h");
        if (cs == null)
            return 0;
        return cs.getKeys(false).size();
    }

    @Override
    boolean deleteHome(OfflinePlayer player, String name) {
        String path = player.getUniqueId() + ".h." + name;
        if (!hc.contains(path))
            return false;
        hc.set(path, null);
        return true;
    }

    @Override
    Map<String, Location> getAllHomes(OfflinePlayer player) {
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

    @Override
    void clearCache() {
        // TODO Auto-generated method stub
    }
}
