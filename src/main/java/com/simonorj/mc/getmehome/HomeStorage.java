package com.simonorj.mc.getmehome;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * This is a superclass(?) for accessing file/database storage of homes.
 * Note: This does not cache anything.  Caching must be done in the
 * main code.
 *
 * @author SimonOrJ
 */
abstract class HomeStorage {
    /**
     * Save to database
     */
    abstract void save();

    /**
     * Gets home of a player in form of Location.
     *
     * @param player Player for UUID
     * @param name   Name of the home
     * @return Location of the player home. null if no such home.
     */
    abstract Location getHome(Player player, String name);

    /**
     * Gets name of the default home.
     * @param player Player to find home of
     * @return home name. null if no (default) home is set.
     */
    abstract String getDefaultHomeName(Player player);

    /**
     * Sets home of a player to their Location.
     * This method overloads setHome(player, name, loc) with loc equal to the current position of the player.
     *
     * @param player Player for UUID and their location
     * @param name   Name of the home
     * @return Success of the saving.
     */
    boolean setHome(Player player, String name) {
        return setHome(player, name, player.getLocation());
    }

    /**
     * Sets a different home as a default home
     * @param player Player name
     * @param name Home name
     * @return if name of home exists
     */
    abstract boolean setDefaultHome(Player player, String name);

    /**
     * Sets home of a player to the specified Location.
     *
     * @param player Player for UUID
     * @param name   Name of the home
     * @param loc    Location of the home
     * @return Success of the saving.
     */
    abstract boolean setHome(Player player, String name, Location loc);

    /**
     * Gets number of homes set by a player
     *
     * @param player Player for UUID
     * @return number of homes
     */
    abstract int getNumberOfHomes(Player player);

    /**
     * Deletes the home of a player.
     *
     * @param player Player for UUID
     * @param name   Name of the home to delete
     * @return Success of the deleting.
     */
    abstract boolean deleteHome(Player player, String name);

    /**
     * Gets a map of every player's homes.
     *
     * @param player Player for UUID
     * @return HashMap of home names to locations.  Empty set if player has no homes.
     * This assumes the specified Player is a valid player, thus it never returns null.
     */
    abstract Map<String, Location> getAllHomes(Player player);

    /**
     * Gets the entire list of homes.  This should be used only for moving the storage method/type.
     *
     * @return an entire map of player homes.
     * @deprecated Better off using "import" instead.
     */
    @Deprecated
    abstract Map<UUID, Map<String, Location>> getEntireList();

    abstract void clearCache();

}
