package com.AgarthaMC.Sinister.mobs.abilities;

import com.AgarthaMC.Sinister.mobs.events.MobEvents;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;

public class SpawnBlockAbility extends Ability {
    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            Location location = MobEvents.getAbilityLocation(entity);
            Block block = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            try {
                block.setType(Material.valueOf(arg.get("type").toUpperCase()));
            } catch(Exception e) {
                System.out.println("[Error] There is no such block");
                return false;
            }
        }
        return true;
    }
}
