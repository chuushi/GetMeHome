package com.simonorj.mc.getmehome;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public final class HereIsYourHome extends JavaPlugin {
	/**
	 * Database or something
	 */
	private HomeStorage storage = null;
	/**
	 * when you get the player's home address
	 */
	private Map<UUID,HashMap<String,Location>> friendz = new HashMap<>();
	/**
	 * "has all homes cached" flag for listing homes / getting all homes set
	 */
	private Set<UUID> knowAllTheirAddress = new HashSet<>();
	
	private String TAG;
	private String HOME;
	private String SET;
	private String DELETE;
	private String DNE;
	private String LIST;
	private String LIMIT_REACHED;
	private String ERROR;
	
	
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
		else if (type.equalsIgnoreCase("MySQL"))
			storage = new HomeSQL(this);
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
		HashMap<String,Location> pHomes = friendz.get(p.getUniqueId());
		
		// Master cache has player and player has home
		if (pHomes != null && pHomes.containsKey(n))
			return pHomes.get(n);
		
		
		// Cache doesn't have player
		if (pHomes == null) {
			pHomes = new HashMap<>();
			friendz.put(p.getUniqueId(), pHomes);
		}
		
		// get from storage
		Location loc = storage.getHome(p, n);
		
		// player has the named home
		if (loc != null)
			pHomes.put(n,loc);
		
		return loc;
	}
	
	private boolean setHome(Player p, String n) {
		if (reachedLimit(p,false))
			return false;
		if (reachedLimit(p,true) && storage.getHome(p, n) == null)
			return false;
		
		if (!storage.setHome(p, n))
			return false;

		// Attempt to get cache
		HashMap<String,Location> pHomes = friendz.get(p.getUniqueId());
		
		// Cache doesn't have player
		if (pHomes == null) {
			pHomes = new HashMap<>();
			friendz.put(p.getUniqueId(), pHomes);
		}
		
		pHomes.put(n,p.getLocation());
		return true;
	}
	
	private boolean deleteHome(Player p, String n) {
		// Delete from storage
		if (!storage.deleteHome(p, n))
			return false;
		// Delete from cache
		friendz.get(p.getUniqueId()).remove(n);
		return true;
	}

	// inclusive: includes currently set home
	private boolean reachedLimit(Player p, boolean inclusive) {
		ConfigurationSection cs = getConfig().getConfigurationSection("limit");
		
		// Get maximum homes
		int maxHomes = -1;
		// Must be ordered: LinkedHashSet.
		LinkedHashSet<String> keys = (LinkedHashSet<String>)cs.getKeys(true);
		Iterator<String> i = keys.iterator();
		// Skip default
		i.next();
		while (i.hasNext()) {
			String node = i.next();
			if(!cs.isInt(node)) // Check if node contains an integer instead of sub-nodes. 
				continue;
			if (p.hasPermission(node)) {
				maxHomes = cs.getInt(node);
				break;
			}
		}
		if (maxHomes == -1)
			maxHomes = cs.getInt("default");
		
		// Get current homes
		int setHomes = listHomes(p).size();
		
		return inclusive ? setHomes > maxHomes
				: setHomes >= maxHomes;
	}
	
	private HashMap<String,Location> listHomes(Player p) {
		final UUID u = p.getUniqueId();
		if (knowAllTheirAddress.contains(u))
			return friendz.get(u);

		HashMap<String,Location> r = storage.getAllHomes(p);
		friendz.put(u,r);
		knowAllTheirAddress.add(u);
		return r;
	}

	private boolean hasError(CommandSender p) {
		final Exception e = storage.getError();
		if (e == null)
			return false;
		p.sendMessage("There was an error.");
		e.printStackTrace();
		return true;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command is for players.");
			return true;
		}
		
		// Player-only area (sethome, delhome, listhomes, home)
		Player p = (Player)sender;

		if (cmd.getName().equalsIgnoreCase("listhomes")) {
			// Get the homes
			HashMap<String,Location> map = listHomes(p);
			
			// Emptiness
			if (map == null || map.size() == 0) {
				p.sendMessage("You have no homes!");
				return true;
			}
			
			Set<String> homes = map.keySet(); 
			
			TextComponent msg = new TextComponent("Your home(s): ");
			
			for (String n : homes) {
				TextComponent tc = new TextComponent("[" + n + "]");
				tc.setClickEvent(new ClickEvent(Action.RUN_COMMAND,"/home " + n));
				msg.addExtra(tc);
				msg.addExtra(" ");
			}
			
			p.spigot().sendMessage(msg);
			return true;
		}
		
		String name = "default";
		if (args.length != 0)
			name = args[0];
		
		if (cmd.getName().equalsIgnoreCase("home")) {
			Location loc = getHome(p,name);
			if (loc == null && !hasError(sender))
				p.sendMessage("home " + name + " is not set.");
			else
				p.teleport(loc);
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("sethome")) {
			if (setHome(p,name))
				p.sendMessage("home " + name + " has been set.");
			else if (!hasError(p))
				p.sendMessage("You have reached the home set limit.");
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("deletehome")) {
			if (deleteHome(p,name))
				p.sendMessage("Your home " + name + "is now gone.");
			else if (!hasError(p))
				p.sendMessage("That home does not exist!");
		}
		
		return false;
	}
}
