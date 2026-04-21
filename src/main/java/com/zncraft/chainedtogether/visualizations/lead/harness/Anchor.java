package com.zncraft.chainedtogether.visualizations.lead.harness;

import java.util.ArrayList;
import java.util.List;

public class Anchor {

    private final ArrayList<Carabiner> carabiners;

    public Anchor(Carabiner... carabiners) {
        this.carabiners = new ArrayList<>(List.of(carabiners));
    }

    public Carabiner getCarabiner(int slot) {
        return carabiners.get(slot);
    }

    public ArrayList<Carabiner> getCarabiners() {
        return carabiners;
    }
}
