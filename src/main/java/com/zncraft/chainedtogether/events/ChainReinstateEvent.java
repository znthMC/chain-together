package com.zncraft.chainedtogether.events;

import com.zncraft.chainedtogether.chain.Chain;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChainReinstateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Chain chain;

    public ChainReinstateEvent(Player player, Chain chain) {
        this.player = player;
        this.chain = chain;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public Chain getChain() {
        return chain;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
