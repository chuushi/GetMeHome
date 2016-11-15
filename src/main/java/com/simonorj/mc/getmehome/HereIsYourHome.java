package com.simonorj.mc.getmehome;

import java.util.HashMap;
//import java.util.HashSet;
import java.util.Map;
//import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class HereIsYourHome extends JavaPlugin {
	/**
	 * Database or something
	 */
	private HomeStorage storage = null;
	/**
	 * when you get the player's home address
	 */
	private Map<UUID,Map<String,Location>> friendz = new HashMap<UUID,Map<String,Location>>();
	/**
	 * "has all homes cached" flag for listing
	 */
	//private Set<UUID> knowAllTheirAddress = new HashSet<UUID>();
	
	@Override
	public void onEnable() {
		// Get config
		saveDefaultConfig();
		
		// Configuration check
		if (!getConfig().contains("storage.type")) {
			getLogger().warning("Storage type cannot be found; is the configuration setup properly?");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		String type = getConfig().getString("storage.type");
		// Storage
		if (type.equalsIgnoreCase("yaml"))
			storage = new HomeYAML(this);
		// TODO: Add MySQL support as well
		
		// Setup Database
		// TODO: Database stuff
		/*
		try {
			db = new HomeSQL("", "", "");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			getLogger().warning("Database driver does not exist. Is Java up to date?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			getLogger().warning("Database connection failed. SQLState/Message:");
			getLogger().warning(e.getSQLState());
			getLogger().warning(e.getMessage());
		}
		*/
		
		if (storage == null) {
			getLogger().warning("GetMeHome: Home storage was not specified correctly. Disabling...");
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	@Override
	public void onDisable() {
	    storage.onDisable();
	}
	
	private Location getHome(Player p, String n) {
		Map<String,Location> pHomes = friendz.get(p.getUniqueId());
		
		// Master cache has player and player has home
		if (pHomes != null && pHomes.containsKey(n))
			return pHomes.get(n);
		
		
		// Cache doesn't have player
		if (pHomes == null) {
			pHomes = new HashMap<String,Location>();
			friendz.put(p.getUniqueId(), pHomes);
		}
		
		// get from storage
		Location loc = storage.getHome(p.getUniqueId(), n);
		
		// player has the named home
		if (loc != null)
			pHomes.put(n,loc);
		
		return loc;
	}
	
	private void setHome(Player p, String n) {
		storage.setHome(p, n, p.getLocation());
		// Attempt to get cache
		Map<String,Location> pHomes = friendz.get(p.getUniqueId());
		
		// Cache doesn't have player
		if (pHomes == null) {
			pHomes = new HashMap<String,Location>();
			friendz.put(p.getUniqueId(), pHomes);
		}
		
		pHomes.put(n,p.getLocation());
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("listhomes")) {
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command is for players.");
			return true;
		}
		
		Player p = (Player)sender;
		String name = "default";
		if (args.length != 0)
			name = args[0];
		
		if (cmd.getName().equalsIgnoreCase("home")) {
			Location loc = getHome(p,name);
			if (loc == null)
				p.sendMessage("home " + name + " is not set.");
			else
				p.teleport(loc);
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("sethome")) {
			setHome(p,name);
			p.sendMessage("home " + name + " has been set.");
			return true;
		}
		
		return false;
	}
}
