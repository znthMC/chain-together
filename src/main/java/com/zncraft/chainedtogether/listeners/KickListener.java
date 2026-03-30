package com.zncraft.chainedtogether.listeners;

import com.zncraft.chainedtogether.ChainedTogether;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public class KickListener implements Listener {

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (event.getReason().equalsIgnoreCase("Flying is not enabled on this server")
                && chainedTogether.getAPI().isChained(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
