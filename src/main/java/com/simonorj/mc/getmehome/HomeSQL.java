package com.simonorj.mc.getmehome;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

final class HomeSQL extends HomeStorage {
	// TODO: WORK IN PROGRESS
	/*
	 * The tables:
	 *
	 * Homes: rowid, pid, homename, world, x, y, z, yaw, pitch, deleteflag
	 * Players: int rowid, varchar name, uuid, int homesset
	 *
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
	

	private static final String SQL_GET = "SELECT ";
	private static final String SQL_SET = "UPDATE";
	private static final String SQL_NEW = "UPDATE";
	private final Connection db;

	// PlaceHolder
	HomeSQL(final HereIsYourHome plugin, boolean server) {
		try {
			String url = null, user = null, password = null;
			if (server) {
					Class.forName("com.mysql.jdbc.Driver");
			} else {
				url = "";
			}
		DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// INCOMPLETE
		throw new RuntimeException();
	}

	@Override
	void onDisable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	Location getHome(Player player, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	boolean setHome(Player player, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	int getHomesSet(Player player) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	boolean deleteHome(Player player, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	HashMap<String, Location> getAllHomes(Player player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	HashMap<UUID, HashMap<String, Location>> getEntireList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Exception getError() {
		// TODO Auto-generated method stub
		return null;
	}
}
