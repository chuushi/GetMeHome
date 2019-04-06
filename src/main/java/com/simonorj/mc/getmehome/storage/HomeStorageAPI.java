package com.simonorj.mc.getmehome.storage;

import com.simonorj.mc.getmehome.GetMeHome;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
public interface HomeStorageAPI {
    default HomeStorageAPI getAPI() {
        GetMeHome p = GetMeHome.getInstance();
        if (p == null)
            return null;

        return p.getStorage();
    }

    /**
     * Save to database
     */
    void save();

    /**
     * Gets UUID from player name
     * @param player exact name of player to look up
     * @return UUID of player
     */
    UUID getUniqueID(String player);

    /**
     * Gets home of a player in form of Location.
     *
     * @param player Player for UUID
     * @param name   Name of the home
     * @return Location of the player home. null if no such home.
     */
    Location getHome(OfflinePlayer player, String name);

    /**
     * Gets name of the default home.
     * @param player Player to find home of
     * @return home name. null if no (default) home is set.
     */
    String getDefaultHomeName(OfflinePlayer player);

    /**
     * Sets a different home as a default home
     * @param player Player name
     * @param name Home name
     * @return if name of home exists
     */
    boolean setDefaultHome(OfflinePlayer player, String name);

    /**
     * Sets home of a player to their Location.
     * This method overloads setHome(player, name, loc) with loc equal to the current position of the player.
     *
     * @param player Player for UUID and their location
     * @param name   Name of the home
     * @return Success of the saving.
     */
    default boolean setHome(Player player, String name) {
        return setHome(player, name, player.getLocation());
    }

    /**
     * Sets home of a player to the specified Location.
     *
     * @param player Player for UUID
     * @param name   Name of the home
     * @param loc    Location of the home
     * @return Success of the saving.
     */
    boolean setHome(OfflinePlayer player, String name, Location loc);

    /**
     * Gets number of homes set by a player
     *
     * @param player Player for UUID
     * @return number of homes
     */
    int getNumberOfHomes(OfflinePlayer player);

    /**
     * Deletes the home of a player.
     *
     * @param player Player for UUID
     * @param name   Name of the home to delete
     * @return Success of the deleting.
     */
    boolean deleteHome(OfflinePlayer player, String name);

    /**
     * Gets a map of every player's homes.
     *
     * @param player Player for UUID
     * @return HashMap of home names to locations.  Empty set if player has no homes.
     * This assumes the specified Player is a valid player, thus it never returns null.
     */
    Map<String, Location> getAllHomes(OfflinePlayer player);

    /**
     * Gets the total number of homes set in the plugin
     *
     * @return total number of homes stored on the storage
     */
    int totalHomes();

    /**
     * Clear cache in case database changed. To be used by
     * SQL storage methods (provisioning)
     */
    void clearCache();
}
