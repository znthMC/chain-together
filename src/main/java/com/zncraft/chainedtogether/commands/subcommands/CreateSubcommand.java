package com.zncraft.chainedtogether.commands.subcommands;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.chain.ChainCreationResult;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CreateSubcommand implements Subcommand {

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Please specify at least two valid players.");
            return true;
        }

        ArrayList<Player> players = new ArrayList<>();
        for (String arg : args) {
            Player target = Bukkit.getPlayer(arg);
            if (target != null) {
                players.add(target);
            }
        }

        for (Player p : players) {
            if (!p.equals(player)) {
                p.teleport(player.getLocation());
            }
        }

        ChainCreationResult result = chainedTogether.getAPI().createChain(players, null);
        switch (result.getResult()) {
            case SUCCESS -> sender.sendMessage("Successfully chained the players together!");
            case NOT_ENOUGH_PLAYERS -> sender.sendMessage("Please specify at least two valid players.");
            case TOO_MANY_PLAYERS -> sender.sendMessage("Too many players specified.");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();
        List<String> alreadyTyped = List.of(args);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!alreadyTyped.contains(player.getName())) {
                suggestions.add(player.getName());
            }
        }
        return suggestions;
    }

    @Override
    public String getName() { return "create"; }

    @Override
    public String getPermission() { return "chainedtogether.create"; }

    @Override
    public String getDescription() { return "Chain players together"; }
}
