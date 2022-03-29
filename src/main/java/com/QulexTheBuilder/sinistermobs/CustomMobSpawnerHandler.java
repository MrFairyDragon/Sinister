package com.QulexTheBuilder.sinistermobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomMobSpawnerHandler implements Listener {

    private static Map<Location, String> locations = new HashMap<>();

    public static void updateSpawnerLocationListFromMySQLDatabase() {
        try {
            Statement statement = Main.getPlugin().connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM CustomSpawnerLocations");
            while(rs.next()) {
                Location generatedLocation = new Location(Bukkit.getServer().getWorld(rs.getString("world")), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
                locations.put(generatedLocation, rs.getString("name"));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void addSpawnerLocationToList(Location location, String name) {
        if(locations.containsKey(location)) {
            removeSpawnerLocationFromList(location);
        }
        try {
            PreparedStatement statement = Main.getPlugin().connection.prepareStatement("INSERT INTO CustomSpawnerLocations(x, y, z, world, name) VALUES ('"+location.getBlockX()+"','"+location.getBlockY()+"','"+location.getBlockZ()+"','"+location.getWorld().getName()+"','"+name+"')");
            statement.executeUpdate();
            locations.put(location, name);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeSpawnerLocationFromList(Location location) {
        if(!(locations.containsKey(location))) {
            return;
        }
        try {
            PreparedStatement statement = Main.getPlugin().connection.prepareStatement("DELETE FROM CustomSpawnerLocations WHERE x='"+location.getBlockX()+"' AND y='"+location.getBlockY()+"' AND z='"+location.getBlockZ()+"' AND world='"+location.getWorld().getName()+"'");
            statement.executeUpdate();
            locations.remove(location);
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    private void onSpawnerBreak(BlockBreakEvent event) {
        if(event.getBlock().getType().equals(Material.SPAWNER)) {
            if(locations.containsKey(event.getBlock().getLocation())) {
                removeSpawnerLocationFromList(event.getBlock().getLocation());
            }
        }
    }

    @EventHandler
    private void onMonsterSpawnerSpawn(SpawnerSpawnEvent event) {
        if(locations.containsKey(event.getSpawner().getLocation())) {
            Main.getPlugin().mobs.changeMob(locations.get(event.getSpawner().getLocation()), event.getEntity());
        }
    }
}
