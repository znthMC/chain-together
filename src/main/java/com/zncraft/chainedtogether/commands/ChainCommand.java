package com.zncraft.chainedtogether.commands;

import com.zncraft.chainedtogether.commands.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChainCommand implements TabExecutor {

    private final Map<String, Subcommand> subcommands = new HashMap<>();

    public ChainCommand() {
        registerSubcommand(new CreateSubcommand());
        registerSubcommand(new LeaveSubcommand());
        registerSubcommand(new ReloadSubcommand());
        registerSubcommand(new InfoSubcommand());
    }

    private void registerSubcommand(Subcommand subcommand) {
        subcommands.put(subcommand.getName().toLowerCase(), subcommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Please specify a subcommand (create, leave, reload, info)");
            return true;
        }

        String subcommandName = args[0].toLowerCase();
        Subcommand subcommand = subcommands.get(subcommandName);

        if (subcommand == null) {
            sender.sendMessage("Unknown subcommand. Use create, leave, reload, or info.");
            return true;
        }

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, args.length - 1);
        return subcommand.execute(sender, subArgs);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Map.Entry<String, Subcommand> entry : subcommands.entrySet()) {
                if (entry.getKey().startsWith(args[0].toLowerCase())) {
                    suggestions.add(entry.getKey());
                }
            }
            return suggestions;
        }

        if (args.length > 1) {
            Subcommand subcommand = subcommands.get(args[0].toLowerCase());
            if (subcommand != null) {
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, args.length - 1);
                return subcommand.tabComplete(sender, subArgs);
            }
        }

        return new ArrayList<>();
    }
}
