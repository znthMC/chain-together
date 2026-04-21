package com.zncraft.chainedtogether.commands.subcommands;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.chain.Chain;
import com.zncraft.chainedtogether.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AddSubcommand implements Subcommand {

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("Usage: /chain add <player>");
            return true;
        }

        if (!chainedTogether.getAPI().isChained(player)) {
            player.sendMessage("You are not part of any chain!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Player not found: " + args[0]);
            return true;
        }

        if (chainedTogether.getAPI().isChained(target)) {
            sender.sendMessage(target.getName() + " is already part of a chain!");
            return true;
        }

        Chain chain = chainedTogether.getAPI().getChain(player);
        if (chain.playerCount() >= ConfigManager.get().getChainMaxPlayers()) {
            sender.sendMessage("The chain is already at its maximum player capacity!");
            return true;
        }

        target.teleport(player.getLocation());
        chain.add(target);
        sender.sendMessage("Added " + target.getName() + " to the chain!");
        target.sendMessage("You have been added to a chain!");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!chainedTogether.getAPI().isChained(player)) {
                    suggestions.add(player.getName());
                }
            }
        }
        return suggestions;
    }

    @Override
    public String getName() { return "add"; }

    @Override
    public String getDescription() { return "Add a player to your chain"; }
}
