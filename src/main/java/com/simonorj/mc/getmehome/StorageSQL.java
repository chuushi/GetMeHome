package com.simonorj.mc.getmehome;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.Map;
import java.util.UUID;

final class StorageSQL extends HomeStorage {
    // TODO: WORK IN PROGRESS
    /*
     * The tables:
     *
     * Homes: int rowid, pid, homename, wid, x, y, z, yaw, pitch, deleteflag
     * Players: int rowid, varchar name, uuid, int homesset, int (rowid of) defaulthome
     * World: int rowid, world
     */

    // From the other place
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
    /*
     * getHome: SELECT  wid,x,y,z,yaw,pitch FROM _homes WHERE pid=$ROWID LIMIT 1;
     * setHome: UPDATE _homes SET blah WHERE pid=$ROWID LIMIT 1;
     *   if not modified:
     *     Look for home with deleteflag
     *       if has:
     *         UPDATE _homes SET blah WHERE
     *
     *     if the above returned nothing:
     *       INSERT INTO _homes
     */

    private final String url;
    private final String username;
    private final String password;
    private final String prefix;

    // PlaceHolder
    StorageSQL(final GetMeHome plugin) throws IllegalStateException, SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the driver in the classpath!", e);
        }

        this.url = "jdbc:mysql://" + plugin.getConfig().getString("storage.hostname") + "/" + plugin.getConfig().getString("storage.database");
        this.username = plugin.getConfig().getString("storage.username");
        this.password = plugin.getConfig().getString("storage.password", "");
        this.prefix = plugin.getConfig().getString("storage.prefix", "gmh_");

        // Test the connection: can throw SQLException
        Connection connection = DriverManager.getConnection(url, username, password);
        connection.close();
    }

    @Override
    void save() {
        // nothing?

    }

    @Override
    UUID getUniqueID(String player) {
        return null;
    }

    @Override
    Location getHome(OfflinePlayer player, String name) {
        String sql = "SELECT defaulthome FROM " + prefix + "blah WHERE uuid = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
        return null;
    }

    @Override
    String getDefaultHomeName(OfflinePlayer player) {
        return null;
    }

    @Override
    boolean setDefaultHome(OfflinePlayer player, String name) {
        return false;
    }

    @Override
    boolean setHome(OfflinePlayer player, String name, Location loc) {
        return false;
    }

    @Override
    int getNumberOfHomes(OfflinePlayer player) {
        return 0;
    }

    @Override
    boolean deleteHome(OfflinePlayer player, String name) {
        return false;
    }

    @Override
    Map<String, Location> getAllHomes(OfflinePlayer player) {
        return null;
    }

    @Override
    Map<UUID, Map<String, Location>> getEntireList() {
        return null;
    }

    @Override
    void clearCache() {

    }


}
