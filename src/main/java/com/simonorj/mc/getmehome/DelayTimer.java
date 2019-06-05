package com.simonorj.mc.getmehome;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class DelayTimer {
    private final Map<Player, CooldownTimer> cooldownMap = new HashMap<>();
    private final Map<Player, WarmupTimer> warmupMap = new HashMap<>();
    private final GetMeHome plugin;

    public DelayTimer(GetMeHome plugin) {
        this.plugin = plugin;
    }

    public boolean cancelWarmup(Player p) {
        WarmupTimer wt = warmupMap.get(p);
        if (wt == null)
            return false;

        wt.incompleteCancel();
        return true;
    }

    public int getCooldown(Player p) {
        CooldownTimer ct = cooldownMap.get(p);
        if (ct == null)
            return 0;

        return ct.counter;
    }

    public void cancelCooldown(Player p) {
        CooldownTimer ct = cooldownMap.remove(p);
        if (ct == null)
            return;

        ct.cancel();
    }

    public void newWarmup(Player p, int counter, boolean checkMovement, Runnable onTime, Runnable onCancel) {
        WarmupTimer wt = new WarmupTimer(p, counter, checkMovement, onTime, onCancel);
        wt.runTaskTimerAsynchronously(plugin, 1L, 1L);
        warmupMap.put(p, wt);
    }

    // I may want to move this and related stuff into another class
    public void newCooldown(Player p, int counter) {
        CooldownTimer tmr = new CooldownTimer(p, counter);
        tmr.runTaskTimerAsynchronously(plugin, 1L, 1L);
        cooldownMap.put(p, tmr);
    }


    private class CooldownTimer extends BukkitRunnable {
        Player player;
        int counter;

        private CooldownTimer(Player p, int counter) {
            this.player = p;
            this.counter = counter;
        }

        @Override
        public void run() {
            if (--counter == 0) {
                cooldownMap.remove(player);
                this.cancel();
            }
        }
    }

    private class WarmupTimer extends BukkitRunnable {
        private final Player sender;
        private final boolean checkMovement;

        private int counter;
        private Location polledLocation;
        private Runnable onTime;
        private Runnable onCancel;

        private WarmupTimer(Player sender, int counter, boolean checkMovement, Runnable onTime, Runnable onCancel) {
            this.sender = sender;
            this.counter = counter;
            this.checkMovement = checkMovement;
            this.onTime = onTime;
            this.onCancel = onCancel;

            this.polledLocation = sender.getLocation();
        }

        @Override
        public void run() {
            if (checkMovement && counter % 10 == 0) {
                Location loc = sender.getLocation();

                if (polledLocation.distanceSquared(loc) >= 0.4) {
                    incompleteCancel();
                    return;
                } else {
                    polledLocation = loc;
                }
            }

            if (--counter == 0) {
                this.cancel();
                warmupMap.remove(sender);

                // Move it back over to main thread
                plugin.getServer().getScheduler().runTask(plugin, onTime);
                // A BukkitRunnable was moved out of here back into HomeCommands.
            }
        }

        private void incompleteCancel() {
            this.cancel();
            warmupMap.remove(sender);
            plugin.getServer().getScheduler().runTask(plugin, onCancel);
        }
    }
}
