package com.zncraft.chainedtogether.chain;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.config.ConfigManager;
import com.zncraft.chainedtogether.events.ChainCreateEvent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChainedTogetherAPI {

    public List<Chain> getChains() {
        return Collections.unmodifiableList(Chain.chains);
    }

    public ChainCreationResult createChain(List<Player> players, ChainConfiguration chainConfiguration) {
        // Filter out already-chained players
        List<Player> participants = new ArrayList<>();
        for (Player player : players) {
            if (!isChained(player)) {
                participants.add(player);
            }
        }

        if (participants.size() < 2) {
            return new ChainCreationResult(ChainCreationResult.Result.NOT_ENOUGH_PLAYERS);
        }
        if (participants.size() > ConfigManager.get().getChainMaxPlayers()) {
            return new ChainCreationResult(ChainCreationResult.Result.TOO_MANY_PLAYERS);
        }

        Chain chain = new Chain(participants, chainConfiguration);
        ChainedTogether.getInstance().getServer().getPluginManager().callEvent(new ChainCreateEvent(chain));
        return new ChainCreationResult(ChainCreationResult.Result.SUCCESS, chain);
    }

    public Chain getChain(Player player) {
        for (Chain chain : getChains()) {
            if (chain.contains(player)) return chain;
        }
        return null;
    }

    public boolean isChained(Player player) {
        return getChain(player) != null;
    }

    public Chain getSameChain(Player a, Player b) {
        Chain chainA = getChain(a);
        Chain chainB = getChain(b);
        if (chainA != null && chainA.equals(chainB)) return chainA;
        return null;
    }

    public boolean isSameChain(Player a, Player b) {
        return getSameChain(a, b) != null;
    }
}
