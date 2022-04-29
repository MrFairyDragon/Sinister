package com.AgarthaMC.Sinister;

import com.AgarthaMC.Sinister.core.events.VillagerDiamondEvent;
import com.AgarthaMC.Sinister.mobs.*;
import com.AgarthaMC.Sinister.mobs.events.AbilityEvents;
import com.AgarthaMC.Sinister.mobs.events.MobEvents;
import com.AgarthaMC.Sinister.mobs.events.MobSpawningAlgorithm;
import com.AgarthaMC.Sinister.serverEvents.potionRainEvent;
import com.AgarthaMC.Sinister.thiefStuff.BlockPlace;
import com.AgarthaMC.Sinister.thiefStuff.ThiefDeath;
import com.AgarthaMC.Sinister.classes.ClassAssignment;
import com.AgarthaMC.Sinister.core.MySQL;
import com.AgarthaMC.Sinister.items.events.CustomItemEvents;
import com.AgarthaMC.Sinister.items.CustomItems;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Main extends JavaPlugin {

    //EXPAND TO ALL CLASSES/PLUGINS
    private static Main plugin;

    //EXPAND TO ALL CLASSES/PLUGINS
    public static Main getPlugin() {
        return plugin;
    }

    public InstantiateMobs mobs; //Mobs
    public Connection connection; //Core
    private MySQL sql; //Core

    public List<ItemStack> stolenItems = new ArrayList<>(); //Thief

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig(); //EXPAND TO ALL CLASSES/PLUGINS
        mobs = new InstantiateMobs(); //Mobs
        sql = new MySQL(); //Core
        connection = Main.getPlugin().sql.connect(); //Core
        CustomItems.updateItemList(); //Items
        CustomMobSpawnerHandler.updateSpawnerLocationListFromMySQLDatabase(); //Mobs
        ClassAssignment.updateList(); //Classes
        Objects.requireNonNull(this.getCommand("Sinister")).setExecutor(new Commands()); //REMOVED/SPLIT
        Objects.requireNonNull(this.getCommand("PotionEvent")).setExecutor(new Commands()); //ServerEvents
        PluginManager pl = getServer().getPluginManager();
        enableEvents(pl);
        //region ServerEvents
        new BukkitRunnable() {
            @Override
            public void run() {
                potionRainEvent.run();
            }
        }.runTaskTimer(this, 0, 5);
        //endregion
    }

    @Override
    public void onDisable() {
        sql.disconnect();
    }

    private void enableEvents(PluginManager pl) {
        pl.registerEvents(new AbilityEvents(), this); //Mobs
        pl.registerEvents(new MobEvents(), this); //Mobs
        pl.registerEvents(new MobSpawningAlgorithm(), this); //Mobs
        pl.registerEvents(new CustomItemEvents(), this); //Items
        pl.registerEvents(new CustomMobSpawnerHandler(), this); //Mobs
        pl.registerEvents(new VillagerDiamondEvent(), this); //Core
        pl.registerEvents(new potionRainEvent(), this); //ServerEvents
        pl.registerEvents(new BlockPlace(), this); //Thief
        pl.registerEvents(new ThiefDeath(), this); //Thief
    }
}
