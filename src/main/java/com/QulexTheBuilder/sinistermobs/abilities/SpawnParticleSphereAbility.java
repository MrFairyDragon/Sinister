package com.QulexTheBuilder.sinistermobs.abilities;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;

public class SpawnParticleSphereAbility extends Ability {

    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
        Particle particle = null;
        int radius = 1;
        int count = 100;
        if(arg.containsKey("type")) {
            particle = Particle.valueOf(arg.get("type").toUpperCase());
        }
        if(arg.containsKey("radius")) {
            radius = Integer.parseInt(arg.get("radius"));
        }
        if(arg.containsKey("count")) {
            count = Integer.parseInt(arg.get("count"));
        }
        for(Entity entity : targets) {
            if(entity == null) {
                System.out.println("Found no target?");
                return false;
            }
            double phi = Math.PI * (3.0 - Math.sqrt(5.0));
            for(int i = 0; i < count; i++) {
                double y = 1 - ((i / (count - 1.0)) * 2.0);
                double yRadius = Math.sqrt(1 - (y * y));
                double theta = phi * i;
                double x = Math.cos(theta) * yRadius;
                double z = Math.sin(theta) * yRadius;
                entity.getWorld().spawnParticle(particle, entity.getLocation().clone().add(x*radius,((y+1)*radius)-(radius/2),z*radius), 0, 0,0,0);
            }
        }
        return true;
    }
}
