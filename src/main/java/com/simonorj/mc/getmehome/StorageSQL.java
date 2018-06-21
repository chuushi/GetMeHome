package com.simonorj.mc.getmehome;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings(value = {"all"})
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

    private static final String SQL_GET_HOME = "SELECT ";
    private static final String SQL_SET = "UPDATE";
    private static final String SQL_NEW = "UPDATE";
    private final Connection db;

    // PlaceHolder
    StorageSQL(final GetMeHome plugin) {
        try {
            String url = null, user = null, password = null;
            Class.forName("com.mysql.jdbc.Driver");
            DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        // INCOMPLETE
        throw new RuntimeException();
    }

    @Override
    void save() {
        // TODO Auto-generated method stub

    }

    @Override
    UUID getUniqueID(String player) {
        return null;
    }

    @Override
    Location getHome(OfflinePlayer player, String name) {
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
