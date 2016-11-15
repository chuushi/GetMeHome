package com.simonorj.mc.getmehome;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

abstract class HomeStorage {
    abstract void onDisable();
	abstract Location getHome(UUID player, String name);
	abstract boolean setHome(Player player, String name, Location location);
	abstract HashMap<String,Location> getPlayerHomes(UUID player);
	abstract HashMap<UUID,HashMap<String,Location>> getEntierList();
}
