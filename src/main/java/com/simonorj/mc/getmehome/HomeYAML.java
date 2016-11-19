package com.simonorj.mc.getmehome;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

final class HomeYAML extends HomeStorage {
    private File homeFile;
    private FileConfiguration hc;
    private HereIsYourHome plugin;

    HomeYAML(HereIsYourHome pl) {
    	plugin = pl;
        saveDefaultHomeConfig();
        try {
			hc.save(homeFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
    
    private void saveDefaultHomeConfig() {
        homeFile = new File(plugin.getDataFolder(), "homes.yml");

        if (!homeFile.exists()) {
            homeFile.getParentFile().mkdirs();
            plugin.saveResource("homes.yml", false);
         }

        hc = new YamlConfiguration();
        try {
            hc.load(homeFile);
        } catch (InvalidConfigurationException | IOException e) {
			e.printStackTrace();
		}
    }

	@Override
	Location getHome(UUID player, String name) {
		ConfigurationSection cs = hc.getConfigurationSection(player.toString() + "." + name);
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
	boolean setHome(Player player, String name, Location location) {
		// Increment when adding another home
		ConfigurationSection cs = hc.getConfigurationSection(player.getUniqueId().toString());
		if (cs == null) {
			cs = hc.createSection(player.getUniqueId().toString());
		}
		
		// Update name
		if (plugin.getConfig().getBoolean("storage.savename"))
			cs.set("n", player.getName());
		
		// Overwrite variable (and home name if it existed)
		cs = cs.createSection(name);
		cs.set("w", location.getWorld().getName());
		cs.set("c", new double[]{location.getX(),location.getY(),location.getZ()});
		cs.set("y", new float[]{location.getYaw(),location.getPitch()});
		return true;
	}

	@Override
	int getHomesSet(Player player) {
		// Size of configuration
		return hc.getConfigurationSection(player.getUniqueId().toString())
				.getKeys(false).size();
	}
	
	@Override
	boolean deleteHome(Player player, String name) {
		String path = player.getUniqueId() + "." + name;
		if (!hc.contains(path))
			return false;
		hc.set(path, null);
		return true;
	}
	
	@Override
	HashMap<String,Location> getAllHomes(UUID player) {
		ConfigurationSection cs = hc.getConfigurationSection(player.toString());
		if (cs == null)
			return null;
		HashMap<String,Location> ret = new HashMap<>();
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
	HashMap<UUID, HashMap<String, Location>> getEntireList() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	Exception getError() {
		// There can never be an error in here.
		return null;
	}
}
