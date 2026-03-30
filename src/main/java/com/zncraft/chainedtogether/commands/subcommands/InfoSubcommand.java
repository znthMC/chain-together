package com.zncraft.chainedtogether.commands.subcommands;

import com.zncraft.chainedtogether.ChainedTogether;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class InfoSubcommand implements Subcommand {

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("§8Version: §7" + chainedTogether.getDescription().getVersion());
        sender.sendMessage("§8Authors: §7" + chainedTogether.getDescription().getAuthors());
        sender.sendMessage("§8Github:§7 https://github.com/zenithexe/ChainedTogether");
        sender.sendMessage("§8Licence: §7GNU AGPL");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() { return "info"; }

    @Override
    public String getDescription() { return "Show plugin information"; }
}
