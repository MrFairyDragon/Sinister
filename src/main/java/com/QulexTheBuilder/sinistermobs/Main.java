package com.QulexTheBuilder.sinistermobs;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.util.Objects;

public final class Main extends JavaPlugin {

    private static Main plugin;

    public static Main getPlugin() {
        return plugin;
    }
    public InstantiateMobs mobs;
    public Connection connection;
    private MySQL sql;

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();
        mobs = new InstantiateMobs();
        sql = new MySQL();
        connection = Main.getPlugin().sql.connect();
        CustomItems.updateItemList();
        CustomMobSpawnerHandler.updateSpawnerLocationListFromMySQLDatabase();
        PluginManager pl = getServer().getPluginManager();
        Objects.requireNonNull(this.getCommand("sinisterMobs")).setExecutor(new Commands());
        Objects.requireNonNull(this.getCommand("PotionEvent")).setExecutor(new Commands());
        pl.registerEvents(new AbilityEvents(), this);
        pl.registerEvents(new MobEvents(), this);
        pl.registerEvents(new MobSpawningAlgorithm(), this);
        pl.registerEvents(new CustomItemEvents(), this);
        pl.registerEvents(new CustomMobSpawnerHandler(), this);
        pl.registerEvents(new VillagerDiamondEvent(), this);
        pl.registerEvents(new potionRainEvent(), this);
        new BukkitRunnable() {
            @Override
            public void run() {
                potionRainEvent.run();
            }
        }.runTaskTimer(this, 0, 5);
    }

    @Override
    public void onDisable() {
        sql.disconnect();
    }
}
