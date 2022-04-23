package com.AgarthaMC.Sinister.abilities;

import com.AgarthaMC.Sinister.InstantiateMobs;
import com.AgarthaMC.Sinister.Main;
import com.AgarthaMC.Sinister.MobEvents;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpawnMobAbility extends Ability {

    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
            for(Entity entity : targets) {
                if(entity == null) {
                    return false;
                }
                boolean success = false;
                Location location = MobEvents.getAbilityLocation(entity);
                for(File file : Objects.requireNonNull(InstantiateMobs.getFilesInDirectory("mobs"))) {
                    if(file.getName().equalsIgnoreCase(arg.get("type") + ".yml")) {
                        Main.getPlugin().mobs.spawnMob(arg.get("type"), location);
                        success = true;
                    }
                }
                if(!success) {
                    try {
                        Objects.requireNonNull(entity.getLocation().getWorld()).spawnEntity(location, EntityType.valueOf(arg.get("type").toUpperCase()));
                    } catch(Exception e) {
                        System.out.println(ChatColor.RED+"[Error] There is no custom or vanilla with this name: " + ChatColor.WHITE+arg);
                        return false;
                    }
                }
            }
            return true;
    }
}
