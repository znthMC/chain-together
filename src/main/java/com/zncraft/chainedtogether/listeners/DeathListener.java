package com.zncraft.chainedtogether.listeners;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.chain.Chain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathListener implements Listener {

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();
    private final Map<UUID, Chain> pendingRespawn = new HashMap<>();

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Chain chain = chainedTogether.getAPI().getChain(player);
        if (chain == null) return;

        chain.suspend(player);
        pendingRespawn.put(player.getUniqueId(), chain);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Chain chain = pendingRespawn.remove(uuid);
        if (chain == null) return;

        Player target = chain.getFirstAlivePlayer(uuid);
        if (target != null) {
            event.setRespawnLocation(target.getLocation());
        } else {
            event.setRespawnLocation(event.getPlayer().getWorld().getSpawnLocation());
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Player respawnedPlayer = Bukkit.getPlayer(uuid);
                if (respawnedPlayer != null && respawnedPlayer.isOnline()) {
                    chain.reinstate(respawnedPlayer);
                }
            }
        }.runTaskLater(chainedTogether, 1);
    }
}
