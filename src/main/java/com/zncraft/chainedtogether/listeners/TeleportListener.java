package com.zncraft.chainedtogether.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.chain.Chain;
import com.zncraft.chainedtogether.config.ConfigManager;

public class TeleportListener implements Listener {

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();
    private final Set<UUID> chainTeleporting = new HashSet<>();

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        // Skip if this teleport was triggered by our own chain teleport
        if (chainTeleporting.contains(player.getUniqueId())) {
            return;
        }

        Chain chain = chainedTogether.getAPI().getChain(player);
        if (chain == null) {
            return;
        }

        switch (event.getCause()) {
            // Portals: handled by onWorldChange instead
            case NETHER_PORTAL:
            case END_PORTAL:
            case END_GATEWAY:
                return;

            // Not real teleports, ignore and let chain pull handle naturally
            case DISMOUNT:
            case EXIT_BED:
                return;

            // Minor positional shifts, let chain pull handle it
            case CHORUS_FRUIT:
            case UNKNOWN:
                return;

            // Spectator mode, don't drag the whole chain
            case SPECTATE:
                return;

            // Intentional teleports
            case PLUGIN:
                return;

            case COMMAND:
            // Ender pearl: configurable behavior
            case ENDER_PEARL:
                String pearlBehavior = ConfigManager.get().getChainPearlBehavior();
                if (pearlBehavior.equals("NORMAL")) {
                    return;
                }
                teleportChain(chain, player, event.getTo());
                break;

            default:
                teleportChain(chain, player, event.getTo());
                break;
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // Skip if this was a chain teleport
        if (chainTeleporting.contains(player.getUniqueId())) {
            return;
        }

        Chain chain = chainedTogether.getAPI().getChain(player);
        if (chain == null) {
            return;
        }

        // Player has fully arrived in the new world — teleport everyone else to them
        teleportChain(chain, player, player.getLocation());
    }

    private void teleportChain(Chain chain, Player teleporter, org.bukkit.Location destination) {
        for (UUID uuid : chain.getActivePlayers()) {
            if (uuid.equals(teleporter.getUniqueId())) {
                continue;
            }
            Player chained = Bukkit.getPlayer(uuid);
            if (chained != null) {
                chainTeleporting.add(uuid);
                chained.teleport(destination);
                chainTeleporting.remove(uuid);
            }
        }
    }
}
