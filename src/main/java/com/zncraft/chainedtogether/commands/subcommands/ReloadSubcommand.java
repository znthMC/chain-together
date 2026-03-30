package com.zncraft.chainedtogether.commands.subcommands;

import com.zncraft.chainedtogether.ChainedTogether;
import com.zncraft.chainedtogether.chain.Chain;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ReloadSubcommand implements Subcommand {

    private final ChainedTogether chainedTogether = ChainedTogether.getInstance();

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        chainedTogether.reloadConfig();
        chainedTogether.saveDefaultConfig();

        FileConfiguration config = chainedTogether.getConfig();

        for (Chain chain : chainedTogether.getAPI().getChains()) {
            if (!chain.hasCustomConfiguration()) {
                chain.getChainConfiguration().setChainLength(config.getDouble("chain-length"));
                chain.getChainConfiguration().setChainElasticity(config.getDouble("chain-elasticity"));
                chain.getChainConfiguration().setChainStretch(config.getDouble("chain-stretch"));
            }
        }

        sender.sendMessage("Plugin configuration has been successfully reloaded!");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public String getName() { return "reload"; }

    @Override
    public String getPermission() { return "chainedtogether.reload"; }

    @Override
    public String getDescription() { return "Reload the plugin configuration"; }
}
