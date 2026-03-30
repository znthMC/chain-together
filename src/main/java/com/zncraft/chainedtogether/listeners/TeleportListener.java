package com.zncraft.chainedtogether.listeners;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.chain.Chain;
import com.zncraft.chainedtogether.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeleportListener implements Listener {

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();
    private final Set<UUID> chainTeleporting = new HashSet<>();

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        // Skip if this teleport was triggered by our own chain teleport
        if (chainTeleporting.contains(player.getUniqueId())) return;

        Chain chain = chainedTogether.getAPI().getChain(player);
        if (chain == null) return;

        TeleportCause cause = event.getCause();

        // Portal teleports: handled by onWorldChange instead
        if (cause == TeleportCause.NETHER_PORTAL
                || cause == TeleportCause.END_PORTAL
                || cause == TeleportCause.END_GATEWAY) return;

        // Chorus fruit: ignore, let normal chain pull handle it
        if (cause == TeleportCause.CHORUS_FRUIT) return;

        // Ender pearl: configurable
        if (cause == TeleportCause.ENDER_PEARL) {
            String pearlBehavior = ConfigManager.get().getChainPearlBehavior();
            if (pearlBehavior.equals("NORMAL")) return;
        }

        // All other cases: teleport all chained players to the destination
        teleportChain(chain, player, event.getTo());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // Skip if this was a chain teleport
        if (chainTeleporting.contains(player.getUniqueId())) return;

        Chain chain = chainedTogether.getAPI().getChain(player);
        if (chain == null) return;

        // Player has fully arrived in the new world — teleport everyone else to them
        teleportChain(chain, player, player.getLocation());
    }

    private void teleportChain(Chain chain, Player teleporter, org.bukkit.Location destination) {
        for (UUID uuid : chain.getActivePlayers()) {
            if (uuid.equals(teleporter.getUniqueId())) continue;
            Player chained = Bukkit.getPlayer(uuid);
            if (chained != null) {
                chainTeleporting.add(uuid);
                chained.teleport(destination);
                chainTeleporting.remove(uuid);
            }
        }
    }
}
