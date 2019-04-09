package com.simonorj.mc.getmehome;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldSaveEvent;

public class SaveListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAllQuit(PlayerQuitEvent e) {
        if (Bukkit.getOnlinePlayers().size() <= 1) {
            // Save home data
            GetMeHome.getInstance().getStorage().save();
            GetMeHome.getInstance().getStorage().clearCache();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void worldSave(WorldSaveEvent e) {
        // Save home data
        GetMeHome.getInstance().getStorage().save();
    }
}
