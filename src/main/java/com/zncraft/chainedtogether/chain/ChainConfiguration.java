package com.zncraft.chainedtogether.chain;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.config.ConfigManager;
import com.zncraft.chainedtogether.visualizations.ChainVisualization;
import com.zncraft.chainedtogether.visualizations.disabled.DisabledImplementation;
import com.zncraft.chainedtogether.visualizations.lead.LeadImplementation;
import com.zncraft.chainedtogether.visualizations.particle.ParticleImplementation;

public class ChainConfiguration {

    private final ChainVisualization chainVisualization;

    private double chainLength;
    private double chainElasticity;
    private double chainStretch;

    public ChainConfiguration() {
        ConfigManager config = ConfigManager.get();

        String vizType = config.getChainVisualization().toUpperCase();
        switch (vizType) {
            case "LEAD":
                if (ChainedTogether.getInstance().getProtocolManager() == null) {
                    ChainedTogether.getInstance().getLogger().severe("ProtocolLib not available, LEAD visualization can't be used. Falling back to DISABLED.");
                    chainVisualization = new DisabledImplementation();
                } else {
                    chainVisualization = new LeadImplementation();
                }
                break;
            case "PARTICLE":
                chainVisualization = new ParticleImplementation();
                break;
            default:
                ChainedTogether.getInstance().getLogger().severe("Invalid visualization specified in config.yml, defaulting to DISABLED.");
                chainVisualization = new DisabledImplementation();
                break;
        }

        chainLength = config.getChainLength();
        chainElasticity = config.getChainElasticity();
        chainStretch = config.getChainStretch();
    }

    public ChainVisualization getChainVisualization() {
        return chainVisualization;
    }

    public double getChainLength() {
        return chainLength;
    }

    public void setChainLength(double chainLength) {
        this.chainLength = chainLength;
    }

    public double getChainElasticity() {
        return chainElasticity;
    }

    public void setChainElasticity(double chainElasticity) {
        this.chainElasticity = chainElasticity;
    }

    public double getChainStretch() {
        return chainStretch;
    }

    public void setChainStretch(double chainStretch) {
        this.chainStretch = chainStretch;
    }
}
