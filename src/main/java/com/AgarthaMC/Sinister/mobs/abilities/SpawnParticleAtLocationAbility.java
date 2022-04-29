package com.AgarthaMC.Sinister.mobs.abilities;

import com.AgarthaMC.Sinister.mobs.events.MobEvents;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import java.util.List;
import java.util.Map;

public class SpawnParticleAtLocationAbility extends Ability {
    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
        Particle particle = null;
        if(arg.containsKey("type")) {
            particle = Particle.valueOf(arg.get("type").toUpperCase());
        }
        if(particle == null) {
            return false;
        }
        for(Entity entity : targets) {
            if(entity == null) {
                System.out.println("Found no target?");
                return false;
            }
            if(!(entity instanceof LivingEntity)) {
                return false;
            }
            Location location = MobEvents.getAbilityLocation(entity);
            location.getWorld().spawnParticle(particle, location, 1);
        }
        return true;
    }
}
