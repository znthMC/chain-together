package com.zncraft.chainedtogether.visualizations.particle;

import com.zncraft.chainedtogether.config.ConfigManager;
import com.zncraft.chainedtogether.visualizations.ChainVisualization;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ParticleImplementation extends ChainVisualization {

    private List<UUID> activePlayers;
    private int maxParticles;
    private int pStartRed, pStartGreen, pStartBlue;
    private int pEndRed, pEndGreen, pEndBlue;
    private float pSize;

    @Override
    public void init(List<UUID> activePlayers) {
        this.activePlayers = activePlayers;
        loadConfig();
    }

    @Override
    public void tick() {
        if (activePlayers == null) return;
        for (int i = 0; i < activePlayers.size() - 1; i++) {
            Player from = Bukkit.getPlayer(activePlayers.get(i));
            Player to   = Bukkit.getPlayer(activePlayers.get(i + 1));
            if (from == null || to == null || from.isDead() || to.isDead()) continue;
            if (!from.getWorld().equals(to.getWorld())) continue;
            drawParticles(from, to);
        }
    }

    @Override
    public void destroy() {
        activePlayers = null;
    }

    @Override
    public void rebuild(List<UUID> activePlayers) {
        this.activePlayers = activePlayers;
    }

    private void loadConfig() {
        ConfigManager config = ConfigManager.get();
        maxParticles = config.getMaxParticles();
        pSize        = config.getParticleSize();
        pStartRed    = config.getStartRed();
        pStartGreen  = config.getStartGreen();
        pStartBlue   = config.getStartBlue();
        pEndRed      = config.getEndRed();
        pEndGreen    = config.getEndGreen();
        pEndBlue     = config.getEndBlue();
    }

    private void drawParticles(Player from, Player to) {
        double distance = from.getLocation().distance(to.getLocation());
        int numParticles = Math.min((int) distance * 10, maxParticles);

        double fromY = from.getLocation().getY() + 0.7;
        double toY   = to.getLocation().getY() + 0.7;

        DustTransition dustTransition = new DustTransition(
                Color.fromRGB(pStartRed, pStartGreen, pStartBlue),
                Color.fromRGB(pEndRed, pEndGreen, pEndBlue),
                pSize
        );

        for (int i = 0; i < numParticles; i++) {
            double t = i / (double) numParticles;
            double x = from.getLocation().getX() + t * (to.getLocation().getX() - from.getLocation().getX());
            double y = fromY + t * (toY - fromY);
            double z = from.getLocation().getZ() + t * (to.getLocation().getZ() - from.getLocation().getZ());
            from.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, x, y, z, 0, 0, 0, 0, 1, dustTransition);
        }
    }
}
