package com.AgarthaMC.Sinister.mobs.abilities;

import com.AgarthaMC.Sinister.Main;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public class SurfaceLocationAbility extends Ability{
    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            int distance = 0;
            if(arg.containsKey("distance")) {
                distance = Integer.parseInt(arg.get("distance"));
            }
            for(int i = 0; i < 40; i++) {
                Location newLocation = entity.getLocation().clone().add(random.nextInt(distance) - distance / 2, random.nextInt(distance) - distance / 2, random.nextInt(distance) - distance / 2);
                if(newLocation.getBlock().isEmpty() && newLocation.clone().add(0, 1, 0).getBlock().isEmpty() && !newLocation.clone().add(0, -1, 0).getBlock().isEmpty()) {
                    entity.getPersistentDataContainer().set(new NamespacedKey(Main.getPlugin(), "abilityPosition"), PersistentDataType.INTEGER_ARRAY, new int[] {newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ()});
                }
            }
        }
        return true;
    }
}
