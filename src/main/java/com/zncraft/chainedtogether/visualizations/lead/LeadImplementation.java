package com.zncraft.chainedtogether.visualizations.lead;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.visualizations.ChainVisualization;
import com.zncraft.chainedtogether.visualizations.lead.harness.Anchor;
import com.zncraft.chainedtogether.visualizations.lead.harness.Carabiner;

public class LeadImplementation extends ChainVisualization {

    private static final double OFFSET_DISTANCE = 0.3;
    private static final double OFFSET_Y = 0.8;
    private static final String TEAM_NAME = "_CARABINER_NO_COLLISION_";

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();
    private final Map<UUID, Anchor> belts = new HashMap<>();
    private List<UUID> activePlayers = new ArrayList<>();
    private ViewerListener viewerListener;

    @Override
    public void init(List<UUID> activePlayers) {
        this.activePlayers = activePlayers;

        belts.clear();
        for (UUID uuid : activePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            belts.put(uuid, createAnchor());
        }

        // Send spawn + metadata + attach packets to all online players
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            sendFullState(viewer);
        }

        // Register viewer listener for join/respawn/world change
        viewerListener = new ViewerListener(this);
        Bukkit.getPluginManager().registerEvents(viewerListener, chainedTogether);
    }

    @Override
    public void tick() {
        for (Map.Entry<UUID, Anchor> entry : belts.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;

            syncCarabinerNoCollisionTeam(player, entry.getValue());

            double yawRad = Math.toRadians(player.getLocation().getYaw() + 180);
            double x = player.getLocation().getX() + (-Math.sin(yawRad) * OFFSET_DISTANCE);
            double y = player.getLocation().getY() + OFFSET_Y;
            double z = player.getLocation().getZ() + (Math.cos(yawRad) * OFFSET_DISTANCE);

            for (Carabiner carabiner : entry.getValue().getCarabiners()) {
                PacketContainer teleportPacket = carabiner.createTeleportPacket(x, y, z);
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    sendPacket(teleportPacket, viewer);
                }
            }
        }
    }

    private void syncCarabinerNoCollisionTeam(Player player, Anchor anchor) {
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getTeam(TEAM_NAME);

        if (team == null) {
            team = scoreboard.registerNewTeam(TEAM_NAME);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }

        for (Carabiner carabiner : anchor.getCarabiners()) {
            String entry = carabiner.getEntityUuid().toString();
            if (!team.hasEntry(entry)) {
                team.addEntry(entry);
            }
        }
    }

    @Override
    public void destroy() {
        // Unregister viewer listener
        if (viewerListener != null) {
            HandlerList.unregisterAll(viewerListener);
            viewerListener = null;
        }

        // Clean up team entries and send destroy packets
        List<Integer> allIds = new ArrayList<>();
        for (Anchor anchor : belts.values()) {
            for (Carabiner carabiner : anchor.getCarabiners()) {
                carabiner.removeFromTeam();
                allIds.add(carabiner.getEntityId());
            }
        }

        if (!allIds.isEmpty()) {
            PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            destroyPacket.getIntLists().write(0, allIds);
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                sendPacket(destroyPacket, viewer);
            }
        }

        belts.clear();
    }

    @Override
    public void rebuild(List<UUID> newActivePlayers) {
        Set<UUID> newActiveSet = new HashSet<>(newActivePlayers);

        // Destroy virtual entities for players no longer active
        Iterator<Map.Entry<UUID, Anchor>> it = belts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Anchor> entry = it.next();
            if (!newActiveSet.contains(entry.getKey())) {
                for (Carabiner carabiner : entry.getValue().getCarabiners()) {
                    carabiner.removeFromTeam();
                    PacketContainer destroyPacket = carabiner.createDestroyPacket();
                    for (Player viewer : Bukkit.getOnlinePlayers()) {
                        sendPacket(destroyPacket, viewer);
                    }
                }
                it.remove();
            }
        }

        // Create virtual entities for newly active players
        for (UUID uuid : newActivePlayers) {
            if (!belts.containsKey(uuid)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                belts.put(uuid, createAnchor());
            }
        }

        this.activePlayers = newActivePlayers;

        // Resend full state to all viewers
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            sendFullState(viewer);
        }
    }

    void sendFullState(Player player) {
        // Send spawn + metadata for all carabiners
        for (Map.Entry<UUID, Anchor> entry : belts.entrySet()) {
            Player owner = Bukkit.getPlayer(entry.getKey());
            if (owner == null) continue;

            double yawRad = Math.toRadians(owner.getLocation().getYaw() + 180);
            double x = owner.getLocation().getX() + (-Math.sin(yawRad) * OFFSET_DISTANCE);
            double y = owner.getLocation().getY() + OFFSET_Y;
            double z = owner.getLocation().getZ() + (Math.cos(yawRad) * OFFSET_DISTANCE);

            for (Carabiner carabiner : entry.getValue().getCarabiners()) {
                sendPacket(carabiner.createSpawnPacket(x, y, z), player);
                sendPacket(carabiner.createMetadataPacket(), player);
                sendPacket(carabiner.createTeleportPacket(x, y, z), player);
            }
        }

        // Send all attach packets
        structure().forEach(pkt -> send(pkt, player));
    }

    private Anchor createAnchor() {
        return new Anchor(new Carabiner(), new Carabiner());
    }

    private PacketContainer attach(int attachedId, int holderId) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
        packet.getIntegers().write(0, attachedId).write(1, holderId);
        return packet;
    }

    private List<RuleBoundPacket> structure() {
        List<RuleBoundPacket> packets = new ArrayList<>();
        if (belts.isEmpty()) return packets;

        for (int i = 0; i < activePlayers.size() - 1; i++) {
            UUID uuidA = activePlayers.get(i);
            UUID uuidB = activePlayers.get(i + 1);
            Anchor anchorA = belts.get(uuidA);
            Anchor anchorB = belts.get(uuidB);
            Player playerA = Bukkit.getPlayer(uuidA);
            Player playerB = Bukkit.getPlayer(uuidB);
            if (anchorA == null || anchorB == null || playerA == null || playerB == null) continue;

            packets.add(new RuleBoundPacket(attach(anchorB.getCarabiner(0).getEntityId(), playerA.getEntityId()), RuleBoundPacket.PacketRule.EXEMPTED, playerB));
            packets.add(new RuleBoundPacket(attach(anchorA.getCarabiner(1).getEntityId(), playerB.getEntityId()), RuleBoundPacket.PacketRule.PERSONAL, playerB));
        }
        return packets;
    }

    private void send(RuleBoundPacket pkt, Player player) {
        switch (pkt.getPacketRule()) {
            case EXEMPTED -> { if (!player.equals(pkt.getPlayer())) sendPacket(pkt.getPacketContainer(), player); }
            case GLOBAL   -> sendPacket(pkt.getPacketContainer(), player);
            case PERSONAL -> { if (player.equals(pkt.getPlayer())) sendPacket(pkt.getPacketContainer(), player); }
        }
    }

    private void sendPacket(PacketContainer packetContainer, Player player) {
        chainedTogether.getProtocolManager().sendServerPacket(player, packetContainer);
    }
}
