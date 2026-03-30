package com.zncraft.chainedtogether.events;

import com.zncraft.chainedtogether.chain.Chain;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChainCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Chain chain;

    public ChainCreateEvent(Chain chain) {
        this.chain = chain;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Chain getChain() {
        return chain;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}