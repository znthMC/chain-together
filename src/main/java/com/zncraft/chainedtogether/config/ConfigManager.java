package com.zncraft.chainedtogether.config;

import com.zncraft.chainedtogether.ChainedTogether;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private static ConfigManager instance;

    // Chain
    private int chainMaxPlayers;
    private double chainLength;
    private double chainStretch;
    private double chainElasticity;
    private String chainVisualization;

    // Teleport
    private String chainPearlBehavior;

    // Particle
    private int maxParticles;
    private float particleSize;
    private int startRed, startGreen, startBlue;
    private int endRed, endGreen, endBlue;

    private ConfigManager() {
        reload();
    }

    public static void load() {
        instance = new ConfigManager();
    }

    public static ConfigManager get() {
        return instance;
    }

    public void reload() {
        FileConfiguration config = ChainedTogether.getInstance().getConfig();

        chainMaxPlayers     = config.getInt("chain-max-players", 100);
        chainLength         = config.getDouble("chain-length", 6.0);
        chainStretch        = config.getDouble("chain-stretch", 0.25);
        chainElasticity     = config.getDouble("chain-elasticity", 0.0475);
        chainVisualization  = config.getString("chain-visualization", "LEAD").toUpperCase();

        chainPearlBehavior  = config.getString("chain-pearl-behavior", "TELEPORT_ALL").toUpperCase();

        maxParticles  = config.getInt("max-particles", 50);
        particleSize  = (float) config.getDouble("particle-size", 1.0);
        startRed      = config.getInt("start_color.red", 128);
        startGreen    = config.getInt("start_color.green", 128);
        startBlue     = config.getInt("start_color.blue", 128);
        endRed        = config.getInt("end_color.red", 0);
        endGreen      = config.getInt("end_color.green", 0);
        endBlue       = config.getInt("end_color.blue", 0);
    }

    public int getChainMaxPlayers()      { return chainMaxPlayers; }
    public double getChainLength()       { return chainLength; }
    public double getChainStretch()      { return chainStretch; }
    public double getChainElasticity()   { return chainElasticity; }
    public String getChainVisualization(){ return chainVisualization; }

    public String getChainPearlBehavior()  { return chainPearlBehavior; }
    public int getMaxParticles()  { return maxParticles; }
    public float getParticleSize(){ return particleSize; }
    public int getStartRed()      { return startRed; }
    public int getStartGreen()    { return startGreen; }
    public int getStartBlue()     { return startBlue; }
    public int getEndRed()        { return endRed; }
    public int getEndGreen()      { return endGreen; }
    public int getEndBlue()       { return endBlue; }
}
