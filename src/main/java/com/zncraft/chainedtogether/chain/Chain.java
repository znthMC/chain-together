package com.zncraft.chainedtogether.chain;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.visualizations.ChainVisualization;
import com.zncraft.chainedtogether.events.ChainDisbandEvent;
import com.zncraft.chainedtogether.events.ChainReinstateEvent;
import com.zncraft.chainedtogether.events.ChainSuspendEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class Chain {

    public static final List<Chain> chains = new ArrayList<>();

    private final UUID uuid = UUID.randomUUID();
    private final List<UUID> originalOrder = new ArrayList<>();
    private final Set<UUID> suspendedPlayers = new HashSet<>();
    private final List<UUID> activePlayers = new ArrayList<>();
    private final ChainConfiguration chainConfiguration;
    private final ChainVisualization chainVisualization;
    private BukkitTask task;
    private boolean customConfiguration;

    public Chain(List<Player> players, ChainConfiguration chainConfiguration) {
        if (chainConfiguration == null) {
            chainConfiguration = new ChainConfiguration();
            customConfiguration = false;
        } else {
            customConfiguration = true;
        }
        this.chainConfiguration = chainConfiguration;

        for (Player player : players) {
            originalOrder.add(player.getUniqueId());
        }
        rebuildActive();

        chainVisualization = chainConfiguration.getChainVisualization();
        chainVisualization.init(activePlayers);

        chains.add(this);
        start();
    }

    public UUID getUuid() {
        return uuid;
    }

    public ChainConfiguration getChainConfiguration() {
        return chainConfiguration;
    }

    public boolean contains(Player player) {
        return originalOrder.contains(player.getUniqueId());
    }

    public boolean isActive(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }

    public boolean isSuspended(Player player) {
        return suspendedPlayers.contains(player.getUniqueId());
    }

    public List<UUID> getOriginalOrder() {
        return Collections.unmodifiableList(originalOrder);
    }

    public List<UUID> getActivePlayers() {
        return Collections.unmodifiableList(activePlayers);
    }

    public int playerCount() {
        return originalOrder.size();
    }

    public int activePlayerCount() {
        return activePlayers.size();
    }

    public boolean hasCustomConfiguration() {
        return customConfiguration;
    }

    public void suspend(Player player) {
        if (!originalOrder.contains(player.getUniqueId()) || suspendedPlayers.contains(player.getUniqueId())) {
            return;
        }
        suspendedPlayers.add(player.getUniqueId());
        rebuildActive();
        chainVisualization.rebuild(activePlayers);

        ChainSuspendEvent event = new ChainSuspendEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void reinstate(Player player) {
        if (!suspendedPlayers.contains(player.getUniqueId())) {
            return;
        }
        suspendedPlayers.remove(player.getUniqueId());
        rebuildActive();
        chainVisualization.rebuild(activePlayers);

        ChainReinstateEvent event = new ChainReinstateEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
    }

    public boolean remove(Player player) {
        if (!originalOrder.contains(player.getUniqueId())) {
            return false;
        }
        originalOrder.remove(player.getUniqueId());
        suspendedPlayers.remove(player.getUniqueId());
        rebuildActive();

        if (originalOrder.size() < 2) {
            ChainDisbandEvent event = new ChainDisbandEvent(this, ChainDisbandEvent.DisbandReason.NOT_ENOUGH_PLAYERS);
            Bukkit.getPluginManager().callEvent(event);
            nuke();
        } else {
            chainVisualization.rebuild(activePlayers);
        }
        return true;
    }

    public void destroy() {
        ChainDisbandEvent event = new ChainDisbandEvent(this, ChainDisbandEvent.DisbandReason.API_CALL);
        Bukkit.getPluginManager().callEvent(event);
        nuke();
    }

    public Player getFirstAlivePlayer(UUID exclude) {
        for (UUID id : activePlayers) {
            if (id.equals(exclude)) continue;
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.isOnline() && !p.isDead()) {
                return p;
            }
        }
        return null;
    }

    private void rebuildActive() {
        activePlayers.clear();
        for (UUID id : originalOrder) {
            if (!suspendedPlayers.contains(id)) {
                activePlayers.add(id);
            }
        }
    }

    private void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                pullPhase();
                chainVisualization.tick();
            }
        }.runTaskTimer(ChainedTogether.getInstance(), 0, 0);
    }

    private static final double MAX_FORCE = 2.0;

    private void pullPhase() {
        double chainLength = chainConfiguration.getChainLength();
        double chainStretch = chainConfiguration.getChainStretch();
        double chainElasticity = chainConfiguration.getChainElasticity();
        double maxDistance = chainLength + chainStretch;

        for (int i = 0; i < activePlayers.size() - 1; i++) {
            Player a = Bukkit.getPlayer(activePlayers.get(i));
            Player b = Bukkit.getPlayer(activePlayers.get(i + 1));
            if (a == null || b == null || a.isDead() || b.isDead()) continue;
            if (!a.getWorld().equals(b.getWorld())) continue;

            double distance = a.getLocation().distance(b.getLocation());
            if (distance > maxDistance) {
                double forceMagnitude = Math.min(chainElasticity * (distance - chainLength), MAX_FORCE);

                Vector aToB = b.getLocation().toVector().subtract(a.getLocation().toVector()).normalize();
                Vector bToA = aToB.clone().multiply(-1);

                applyForce(a, aToB.multiply(forceMagnitude));
                applyForce(b, bToA.multiply(forceMagnitude));
            }
        }
    }

    private void applyForce(Player player, Vector force) {
        if (player.isInsideVehicle() && player.getVehicle() != null) {
            player.getVehicle().setVelocity(player.getVehicle().getVelocity().clone().add(force));
        } else {
            player.setVelocity(player.getVelocity().clone().add(force));
        }
    }

    private void nuke() {
        task.cancel();
        chainVisualization.destroy();
        chains.remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chain that = (Chain) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
