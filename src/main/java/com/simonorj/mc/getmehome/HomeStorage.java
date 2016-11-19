package com.simonorj.mc.getmehome;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

abstract class HomeStorage {
	/**
	 * Run before shutting down the server.
	 */
    abstract void onDisable();

	/**
	 * Gets home of a player in form of Bukkit's Location class.
	 * @param player
	 * @param name
	 * @return Location of the player home. null if no such home.
	 */
	abstract Location getHome(UUID player, String name);

	/**
	 * Sets home of a player from Bukkit's Location class format.
	 * @param player
	 * @param name
	 * @param location
	 * @return Success of the saving.
	 */
	abstract boolean setHome(Player player, String name, Location location);
	
	/**
	 * Gets number of homes set by a player
	 * @param player
	 * @return number of homes 
	 */
	abstract int getHomesSet(Player player);
	
	/**
	 * Deletes the home of a player.
	 * @param player
	 * @param name
	 * @return Success of the deleting.
	 */
	abstract boolean deleteHome(Player player, String name);

	/**
	 * Gets a map of every player's homes.
	 * @param player
	 * @return HashMap of home names to locations.  Empty set if player has no homes.
	 */
	abstract HashMap<String,Location> getAllHomes(UUID player);

	/**
	 * Gets the entire list of homes.  This should be used only for moving the storage method/type.
	 * @return an entire map of player homes.
	 */
	abstract HashMap<UUID,HashMap<String,Location>> getEntireList();

	/**
	 * Checks if the plugin can connect to the storage/database server/file. Can be called every time an action fails.
	 * @return Error exception
	 */
	abstract Exception getError();
}
