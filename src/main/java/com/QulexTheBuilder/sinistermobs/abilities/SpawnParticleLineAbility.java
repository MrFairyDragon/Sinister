package com.QulexTheBuilder.sinistermobs.abilities;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

public class SpawnParticleLineAbility extends Ability {


    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {


        Particle particle = null;
        int amount = 0;
        int count = 1;
        for(Map.Entry entry : arg.entrySet()) {
            String key = entry.getKey().toString();
            switch(key) {
                case "type":
                    particle = Particle.valueOf(arg.get("type").toUpperCase());
                    break;
                case "amount":
                    amount = Integer.parseInt(arg.get("amount"));
                    break;
                case "count":
                    count = Integer.parseInt(arg.get("count"));
                    break;
            }

        }
        if(amount == 0 || particle == null) {
            return false;
        }
        for(Entity entity : targets) {
            if(entity == null) {
                System.out.println("Found no target?");
                return false;
            }
            Vector vec = entity.getLocation().toVector().subtract(self.getLocation().toVector()).multiply((double)1/amount);
            for(int i = 0; i < amount+1; i++) {
                self.getWorld().spawnParticle(particle, self.getLocation().add(0,1,0).add(vec.clone().multiply(i)), count);
            }
        }
        return true;
    }
}
