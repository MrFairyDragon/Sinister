package com.AgarthaMC.Sinister;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.*;

import java.io.File;
import java.util.*;

public class MobSpawningAlgorithm implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onMonsterSpawns(CreatureSpawnEvent event) {
        if(!(event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL))) {
            return;
        }
        Location centerLocation = new Location(event.getLocation().getWorld(), 0, 0, 0);
        Location location = event.getEntity().getLocation();
        double distance = getDistanceBetweenBlocks(centerLocation.getBlock(), location.getBlock());
        int weightSum = 0;
        Map<String, Integer> mobs = new HashMap<>();
        for(File file : Objects.requireNonNull(InstantiateMobs.getFilesInDirectory("mobs"))) {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            List<Integer> data = yml.getIntegerList("spawnRequirement");
            if(data.size() != 3) {
                return;
            } else {
                int min = data.get(0);
                int max = data.get(1);
                int weight = data.get(2);
                String type = yml.getString("type");
                if(event.getEntity().getType().equals(EntityType.valueOf(Objects.requireNonNull(type).toUpperCase()))) {
                    if(min < distance && distance < max) {
                        mobs.put(file.getName(), weight);
                    }
                }
            }
        }
        if(mobs.isEmpty()) {
            return;
        }
        for(Map.Entry<String, Integer> entry : mobs.entrySet()) {
            weightSum += entry.getValue();
        }
        int roll = random.nextInt(weightSum)+1;
        int counter = 0;
        for(Map.Entry<String, Integer> entry : mobs.entrySet()) {
            if(roll > entry.getValue() + counter) {
                counter += entry.getValue();
            } else {
                Main.getPlugin().mobs.changeMob(entry.getKey().substring(0, entry.getKey().length()-4), event.getEntity());
                return;
            }
        }
    }

    @NotNull
    private double getDistanceBetweenBlocks(Block center, Block location) {
        int distX = Math.abs(center.getX() - location.getX());
        int distZ = Math.abs(center.getZ() - location.getZ());
        return Math.hypot(distX, distZ);
    }
}
