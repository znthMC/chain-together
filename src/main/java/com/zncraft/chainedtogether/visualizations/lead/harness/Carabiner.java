package com.zncraft.chainedtogether.visualizations.lead.harness;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Carabiner {

    private static final AtomicInteger VIRTUAL_ENTITY_ID = new AtomicInteger(Integer.MAX_VALUE / 2);
    private static final String TEAM_NAME = "chained_nocollide";

    // NMS reflection — needed because 1.21.2+ changed SPAWN_ENTITY (velocity Vec3)
    // and ENTITY_TELEPORT (PositionMoveRotation) packet structures
    private static final Class<?> VEC3_CLASS;
    private static final Object VEC3_ZERO;
    private static final Constructor<?> VEC3_CONSTRUCTOR;
    private static final Constructor<?> POSITION_MOVE_ROTATION_CONSTRUCTOR;
    private static final Constructor<?> TELEPORT_PACKET_CONSTRUCTOR;

    static {
        try {
            VEC3_CLASS = Class.forName("net.minecraft.world.phys.Vec3");
            VEC3_ZERO = VEC3_CLASS.getField("ZERO").get(null);
            VEC3_CONSTRUCTOR = VEC3_CLASS.getConstructor(double.class, double.class, double.class);

            Class<?> pmrClass = Class.forName("net.minecraft.world.entity.PositionMoveRotation");
            POSITION_MOVE_ROTATION_CONSTRUCTOR = pmrClass.getConstructor(VEC3_CLASS, VEC3_CLASS, float.class, float.class);

            Class<?> teleportClass = Class.forName("net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket");
            TELEPORT_PACKET_CONSTRUCTOR = teleportClass.getConstructor(int.class, pmrClass, Set.class, boolean.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize NMS reflection for virtual entity packets", e);
        }
    }

    private final int entityId;
    private final UUID entityUuid;

    public Carabiner() {
        this.entityId = VIRTUAL_ENTITY_ID.decrementAndGet();
        this.entityUuid = UUID.randomUUID();
        getOrCreateTeam().addEntry(entityUuid.toString());
    }

    public int getEntityId() {
        return entityId;
    }

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public PacketContainer createSpawnPacket(double x, double y, double z) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, entityId);
        packet.getUUIDs().write(0, entityUuid);
        packet.getEntityTypeModifier().write(0, EntityType.RABBIT);
        packet.getDoubles().write(0, x).write(1, y).write(2, z);
        // Set velocity Vec3 to zero (null causes NPE during packet encoding)
        packet.getModifier().withType(VEC3_CLASS).write(0, VEC3_ZERO);
        return packet;
    }

    public PacketContainer createMetadataPacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getIntegers().write(0, entityId);

        List<WrappedDataValue> metadata = new ArrayList<>();
        // Index 0 = shared entity flags byte. Bit 0x20 = invisible
        metadata.add(new WrappedDataValue(0,
                WrappedDataWatcher.Registry.get(Byte.class),
                (byte) 0x20));

        packet.getDataValueCollectionModifier().write(0, metadata);
        return packet;
    }

    public PacketContainer createTeleportPacket(double x, double y, double z) {
        // 1.21.2+ uses PositionMoveRotation instead of raw doubles — must construct via NMS
        try {
            Object position = VEC3_CONSTRUCTOR.newInstance(x, y, z);
            Object pmr = POSITION_MOVE_ROTATION_CONSTRUCTOR.newInstance(position, VEC3_ZERO, 0f, 0f);
            Object nmsPacket = TELEPORT_PACKET_CONSTRUCTOR.newInstance(entityId, pmr, Set.of(), false);
            return PacketContainer.fromPacket(nmsPacket);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create teleport packet", e);
        }
    }

    public void removeFromTeam() {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(TEAM_NAME);
        if (team != null) team.removeEntry(entityUuid.toString());
    }

    public PacketContainer createDestroyPacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntLists().write(0, List.of(entityId));
        return packet;
    }

    private static Team getOrCreateTeam() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.registerNewTeam(TEAM_NAME);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        return team;
    }
}
