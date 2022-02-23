package com.QulexTheBuilder.sinistermobs;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {

    private static Main plugin;

    public static Main getPlugin() {
        return plugin;
    }
    public InstantiateMobs mobs;

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();
        mobs = new InstantiateMobs();
        CustomItems.updateItemList();
        Objects.requireNonNull(this.getCommand("sinisterMobs")).setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new AbilityEvents(), this);
        getServer().getPluginManager().registerEvents(new MobEvents(), this);
        getServer().getPluginManager().registerEvents(new MobSpawningAlgorithm(), this);
    }

    @Override
    public void onDisable() {

    }
}
