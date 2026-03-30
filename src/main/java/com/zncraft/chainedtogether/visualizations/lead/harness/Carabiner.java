package com.zncraft.chainedtogether.visualizations.lead.harness;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Rabbit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Carabiner {

    private static final String TEAM_NAME = "chained_nocollide";
    private final Entity carabiner;

    public Carabiner(Location location) {
        Rabbit rabbit = (Rabbit) location.getWorld().spawnEntity(location, EntityType.RABBIT);
        rabbit.setAI(false);
        rabbit.setGravity(false);
        rabbit.setInvisible(true);
        rabbit.setInvulnerable(true);
        rabbit.setCustomNameVisible(false);
        rabbit.setSilent(true);
        rabbit.setAware(false);
        rabbit.setLootTable(null);
        rabbit.setBaby();
        rabbit.setAgeLock(true);
        rabbit.setPersistent(false);
        rabbit.setCollidable(false);
        rabbit.setCustomName("chainedtogetheraeaeaeae");
        getOrCreateTeam().addEntry(rabbit.getUniqueId().toString());
        this.carabiner = rabbit;
    }

    public void nuke() {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(TEAM_NAME);
        if (team != null) team.removeEntry(carabiner.getUniqueId().toString());
        carabiner.remove();
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

    public Entity getCarabiner() {
        return carabiner;
    }
}
