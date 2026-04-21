package com.zncraft.chainedtogether.commands.subcommands;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.chain.Chain;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BreakSubcommand implements Subcommand {

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        if (!chainedTogether.getAPI().isChained(player)) {
            player.sendMessage("You are not part of any chain!");
            return true;
        }

        Chain chain = chainedTogether.getAPI().getChain(player);
        chain.destroy();
        player.sendMessage("The chain has been broken!");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() { return "break"; }

    @Override
    public String getDescription() { return "Break the chain and remove all players from it"; }
}
