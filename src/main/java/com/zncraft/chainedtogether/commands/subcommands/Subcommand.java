package com.zncraft.chainedtogether.commands.subcommands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface Subcommand {
    boolean execute(CommandSender sender, String[] args);
    List<String> tabComplete(CommandSender sender, String[] args);
    String getName();
    String getDescription();
}
