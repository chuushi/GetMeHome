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
	 * Players: rowid, name, uuid, homesset
	 *
	 */
	private static final String SQL_GET = "SELECT ";
	private static final String SQL_SET = "UPDATE";
	private static final String SQL_NEW = "UPDATE";
	private final Connection db;

	HomeSQL(final String url, final String username, final String password) throws ClassNotFoundException, SQLException {
		
		// Start the database
		if (username == null) {
			Class.forName(""); // TODO: SQLite driver
			db = DriverManager.getConnection(url);
		} else {
			Class.forName("com.mysql.jdbc.Driver");
			db = DriverManager.getConnection(url, username, password);
		}
		
		// Check if table exists
		Statement s = db.prepareStatement("SHOW TABLES LIKE ?");
		
		
		// No table?
		//setupTable();
	}
	
	Location HomeDB(String homeName) {
		return null;
	}

	@Override
	void onDisable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	Location getHome(UUID player, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	boolean setHome(Player player, String name, Location location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	HashMap<String, Location> getPlayerHomes(UUID player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	HashMap<UUID, HashMap<String, Location>> getEntireList() {
		// TODO Auto-generated method stub
		return null;
	}
}
