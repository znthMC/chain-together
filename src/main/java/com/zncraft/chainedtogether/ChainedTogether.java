package com.zncraft.chainedtogether;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.zncraft.chainedtogether.chain.ChainedTogetherAPI;
import com.zncraft.chainedtogether.commands.ChainCommand;
import com.zncraft.chainedtogether.config.ConfigManager;
import com.zncraft.chainedtogether.listeners.DeathListener;
import com.zncraft.chainedtogether.listeners.KickListener;
import com.zncraft.chainedtogether.listeners.QuitListener;
import com.zncraft.chainedtogether.listeners.TeleportListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChainedTogether extends JavaPlugin {

    private static ChainedTogether instance;
    private ChainedTogetherAPI api;
    private ProtocolManager protocolManager;

    public static ChainedTogether getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        ConfigManager.load();

        api = new ChainedTogetherAPI();

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            protocolManager = ProtocolLibrary.getProtocolManager();
        }

        ChainCommand chainCommand = new ChainCommand();
        getCommand("chain").setExecutor(chainCommand);
        getCommand("chain").setTabCompleter(chainCommand);

        Bukkit.getPluginManager().registerEvents(new QuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new KickListener(), this);
        Bukkit.getPluginManager().registerEvents(new TeleportListener(), this);
    }

    @Override
    public void onDisable() {
    }

    public ChainedTogetherAPI getAPI() {
        return api;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
