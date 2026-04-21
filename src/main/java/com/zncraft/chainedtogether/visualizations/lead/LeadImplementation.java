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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.visualizations.ChainVisualization;
import com.zncraft.chainedtogether.visualizations.lead.harness.Anchor;
import com.zncraft.chainedtogether.visualizations.lead.harness.Carabiner;

public class LeadImplementation extends ChainVisualization {

    // The scoreboard team is essential, so that the carabiner doesn't collide with the player
    private static final String TEAM_NAME = "_CARABINER_NO_COLLISION_";
    
    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();
    private final Map<UUID, Anchor> belts = new HashMap<>();
    private List<UUID> activePlayers = new ArrayList<>();
    private PacketListener packetListener;

    @Override
    public void init(List<UUID> activePlayers) {
        this.activePlayers = activePlayers;

        belts.clear();
        for (UUID uuid : activePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            belts.put(uuid, createAnchor(player));
        }

        List<RuleBoundPacket> structure = structure();
        Bukkit.getOnlinePlayers().forEach(p -> structure.forEach(pkt -> send(pkt, p)));

        packetListener = new PacketAdapter(chainedTogether, ListenerPriority.NORMAL, PacketType.Play.Server.SPAWN_ENTITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                UUID uuid = event.getPacket().getUUIDs().read(0);
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null && entity.getType().equals(EntityType.RABBIT)
                        && "chainedcarabiner".equals(entity.getCustomName())) {
                    refreshPackets(event.getPlayer());
                }
            }
        };
        chainedTogether.getProtocolManager().addPacketListener(packetListener);
    }

    @Override
    public void tick() {
        for (Map.Entry<UUID, Anchor> entry : belts.entrySet()) {
            
            Player player = Bukkit.getPlayer(entry.getKey());

            if (player == null) continue;
            
            //This is to abvoid collision of the player with the carabiner.
            syncCarabinerNoCollisionTeam(player, entry.getValue());
            
            double yawRad = Math.toRadians(player.getLocation().getYaw() + 180);
            
            for (Carabiner carabiner : entry.getValue().getCarabiners()) {
                carabiner.getCarabiner().teleport(
                        player.getLocation().clone().add(
                                -Math.sin(yawRad) * 0.3, 0.8, Math.cos(yawRad) * 0.3
                        )
                );
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
            String entry = carabiner.getCarabiner().getUniqueId().toString();
            if (!team.hasEntry(entry)) {
                team.addEntry(entry);
            }
        }
    }

    @Override
    public void destroy() {
        if (packetListener != null) {
            chainedTogether.getProtocolManager().removePacketListener(packetListener);
            packetListener = null;
        }
        for (Anchor anchor : belts.values()) {
            anchor.getCarabiners().forEach(Carabiner::nuke);
        }
        belts.clear();
    }

    @Override
    public void rebuild(List<UUID> newActivePlayers) {
        Set<UUID> newActiveSet = new HashSet<>(newActivePlayers);

        // Remove anchors for players no longer active
        Iterator<Map.Entry<UUID, Anchor>> it = belts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Anchor> entry = it.next();
            if (!newActiveSet.contains(entry.getKey())) {
                entry.getValue().getCarabiners().forEach(Carabiner::nuke);
                it.remove();
            }
        }

        // Add anchors for newly active players
        for (UUID uuid : newActivePlayers) {
            if (!belts.containsKey(uuid)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                belts.put(uuid, createAnchor(player));
            }
        }

        this.activePlayers = newActivePlayers;
        List<RuleBoundPacket> structure = structure();
        Bukkit.getOnlinePlayers().forEach(p -> structure.forEach(pkt -> send(pkt, p)));
    }

    private void refreshPackets(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                structure().forEach(pkt -> send(pkt, player));
            }
        }.runTaskLater(chainedTogether, 1);
    }

    private Anchor createAnchor(Player player) {
        return new Anchor(new Carabiner(player.getLocation()), new Carabiner(player.getLocation()));
    }

    private PacketContainer attach(Entity attached, Entity holder) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
        packet.getIntegers().write(0, attached.getEntityId()).write(1, holder.getEntityId());
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

            packets.add(new RuleBoundPacket(attach(anchorB.getCarabiner(0), playerA), RuleBoundPacket.PacketRule.EXEMPTED, playerB));
            packets.add(new RuleBoundPacket(attach(anchorA.getCarabiner(1), playerB), RuleBoundPacket.PacketRule.PERSONAL, playerB));
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
