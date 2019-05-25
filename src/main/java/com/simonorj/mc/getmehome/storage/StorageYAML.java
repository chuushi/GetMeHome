package com.simonorj.mc.getmehome.storage;

import com.simonorj.mc.getmehome.ConfigTool;
import com.simonorj.mc.getmehome.GetMeHome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StorageYAML implements HomeStorageAPI {
    private final GetMeHome plugin = GetMeHome.getInstance();
    private final boolean saveName = plugin.getConfig().getBoolean(ConfigTool.STORAGE_SAVENAME_NODE);
    private final File homeFile = new File(plugin.getDataFolder(), "homes.yml");
    private final FileConfiguration storage = new YamlConfiguration();
    private boolean updateFlag = false;

    /*
     * Home structure:
     * names:
     *   NAME: UUID
     *
     * UUID:
     *   n: PLAYER NAME (if enabled)
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

    private String cleanName(String s) {
        return s.replaceAll("\\.", "-");
    }

    public StorageYAML() {
        if (!homeFile.exists()) {
            plugin.saveResource("homes.yml", false);
        }

        clearCache();
    }

    @Override
    public void save() {
        if (updateFlag) {
            try {
                storage.save(homeFile);
                updateFlag = false;
            } catch (IOException e) {
                plugin.getLogger().warning("Homes failed to save!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public UUID getUniqueID(String player) {
        String uuid = storage.getString("names." + player.toLowerCase());
        if (uuid == null)
            return null;
        return UUID.fromString(uuid);
    }

    @Override
    public Location getHome(UUID uuid, String name) {
        ConfigurationSection cs = storage.getConfigurationSection(uuid.toString() + ".h." + cleanName(name));
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
    public String getDefaultHomeName(UUID uuid) {
        String ret = storage.getString(uuid.toString() + ".d");
        if (ret == null)
            return "default";
        return ret;
    }

    @Override
    public boolean setDefaultHome(UUID uuid, String name) {
        name = cleanName(name);
        updateFlag = true;

        if (storage.getConfigurationSection(uuid.toString() + ".h." + name) == null)
            return false;
        storage.set(uuid.toString() + ".d", name);
        return true;
    }

    @Override
    public boolean setHome(UUID uuid, String name, Location loc) {
        updateFlag = true;

        String uid = uuid.toString();
        // Increment when adding another home
        ConfigurationSection cs = storage.getConfigurationSection(uid);
        if (cs == null) {
            cs = storage.createSection(uid);
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);

        // Update name
        storage.set("names." + p.getName().toLowerCase(), uuid.toString());
        if (saveName)
            cs.set("n", p.getName());

        cs = cs.getConfigurationSection("h");
        if (cs == null) {
            cs = storage.createSection(uid + ".h");
        }

        // Overwrite variable (and home name if it existed)
        cs = cs.createSection(cleanName(name));
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
    public int getNumberOfHomes(UUID uuid) {
        // Size of configuration
        ConfigurationSection cs = storage.getConfigurationSection(uuid + ".h");
        if (cs == null)
            return 0;
        return cs.getKeys(false).size();
    }

    @Override
    public boolean deleteHome(UUID uuid, String name) {
        String path = uuid + ".h." + cleanName(name);
        if (!storage.contains(path))
            return false;
        storage.set(path, null);
        return true;
    }

    @Override
    public Map<String, Location> getAllHomes(UUID uuid) {
        ConfigurationSection cs = storage.getConfigurationSection(uuid + ".h");
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
    public int totalHomes() {
        int ret = 0;
        for (String k : storage.getKeys(false)) {
            if (k.length() != 36)
                continue;
            ret += storage
                    .getConfigurationSection(k + ".h")
                    .getKeys(false)
                    .size();
        }
        return ret;
    }

    @Override
    public void clearCache() {
        try {
            storage.load(homeFile);
            storage.save(homeFile);
            updateFlag = false;
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }
}
