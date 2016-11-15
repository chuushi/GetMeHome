package com.simonorj.mc.getmehome;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Location;
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
	boolean setHome(Player player, String name, Location location) {
		String uuid = player.getUniqueId() + ".";
		if (plugin.getConfig().getBoolean("storage.savename"))
			hc.set(uuid + "n", player.getName());
		hc.set(uuid + name + ".w", location.getWorld().getName());
		hc.set(uuid + name + ".c", new double[]{location.getX(),location.getY(),location.getZ()});
		hc.set(uuid + name + ".y", new float[]{location.getYaw(),location.getPitch()});
		// TODO: See if the above works.
		/*
		hc.set(uuid + name + ".x", location.getX());
		hc.set(uuid + name + ".y", location.getY());
		hc.set(uuid + name + ".z", location.getZ());
		hc.set(uuid + name + ".yaw", location.getYaw());
		hc.set(uuid + name + ".pch", location.getPitch());
		*/
		return true;
	}

	@Override
	Location getHome(UUID player, String name) {
		StringBuilder nb = new StringBuilder(player.toString())
				.append('.')
				.append(name);
		
		if (!hc.contains(nb.toString()))
			return null;
		
		String n = nb.append('.').toString();
		
		Iterator<Double> c = hc.getDoubleList(n+"c").iterator();
		Iterator<Float> y = hc.getFloatList(n+"y").iterator();
		
		return new Location(
				plugin.getServer().getWorld(hc.getString(n+"w")),
				c.next(),
				c.next(),
				c.next(),
				y.next(),
				y.next()
				/*
				hc.getDouble(n+"x"),
				hc.getDouble(n+"y"),
				hc.getDouble(n+"z"),
				(float)hc.getDouble(n+"yaw"),
				(float)hc.getDouble(n+"pch")
				*/
				);
	}
	
	@Override
	HashMap<String, Location> getPlayerHomes(UUID player) {
		// TODO: do this
		if (!hc.contains(player.toString()))
			return null;
		return null;
		
	}
	
	@Override
	HashMap<UUID, HashMap<String, Location>> getEntierList() {
		// TODO Auto-generated method stub
		return null;
	}
}
