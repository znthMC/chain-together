package com.zncraft.chainedtogether.listeners;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.chain.Chain;
import com.zncraft.chainedtogether.events.ChainLeaveEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Chain chain = chainedTogether.getAPI().getChain(event.getPlayer());
        if (chain != null) {
            ChainLeaveEvent chainLeaveEvent = new ChainLeaveEvent(event.getPlayer(), chain, ChainLeaveEvent.LeaveReason.DISCONNECT);
            chainedTogether.getServer().getPluginManager().callEvent(chainLeaveEvent);
            chain.remove(event.getPlayer());
        }
    }
}
