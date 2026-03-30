package com.zncraft.chainedtogether.visualizations;

import java.util.List;
import java.util.UUID;

public abstract class ChainVisualization {

    public abstract void init(List<UUID> activePlayers);

    public abstract void tick();

    public abstract void destroy();

    public abstract void rebuild(List<UUID> activePlayers);
}
