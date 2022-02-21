package com.QulexTheBuilder.sinistermobs.abilities;

import com.QulexTheBuilder.sinistermobs.InstantiateMobs;
import com.QulexTheBuilder.sinistermobs.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpawnMountAbility extends Ability {

    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            if(entity.isInsideVehicle()) {
                return false;
            }
            boolean success = false;
            for(File file : Objects.requireNonNull(InstantiateMobs.getFilesInDirectory("mobs"))) {
                if(file.getName().equalsIgnoreCase(arg.get("type") + ".yml")) {
                    YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                    Entity spawnedEntity = Objects.requireNonNull(entity.getLocation().getWorld()).spawnEntity(entity.getLocation(), EntityType.valueOf(yml.getString("type").toUpperCase()));
                    spawnedEntity.addPassenger(entity);
                    Main.getPlugin().mobs.changeMob(arg.get("type"), spawnedEntity);
                    success = true;
                }
            }
            if(!success) {
                try {
                    Entity spawnedEntity = Objects.requireNonNull(entity.getLocation().getWorld()).spawnEntity(entity.getLocation(), EntityType.valueOf(arg.get("type").toUpperCase()));
                    spawnedEntity.addPassenger(entity);
                } catch(Exception e) {
                    System.out.println(ChatColor.RED + "[Error] There is no custom or vanilla with this name: " + ChatColor.WHITE + arg);
                    return false;
                }
            }
        }
        return true;
    }
}
