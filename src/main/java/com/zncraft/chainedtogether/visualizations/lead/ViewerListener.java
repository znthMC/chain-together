package com.zncraft.chainedtogether.visualizations.lead;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.zncraft.chainedtogether.ChainedTogether;

public class ViewerListener implements Listener {

    private final LeadImplementation leadImplementation;

    public ViewerListener(LeadImplementation leadImplementation) {
        this.leadImplementation = leadImplementation;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOnline()) {
                    leadImplementation.sendFullState(event.getPlayer());
                }
            }
        }.runTaskLater(ChainedTogether.getInstance(), 2);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOnline()) {
                    leadImplementation.sendFullState(event.getPlayer());
                }
            }
        }.runTaskLater(ChainedTogether.getInstance(), 2);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOnline()) {
                    leadImplementation.sendFullState(event.getPlayer());
                }
            }
        }.runTaskLater(ChainedTogether.getInstance(), 2);
    }
}
