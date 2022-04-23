package com.AgarthaMC.Sinister.abilities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Map;

public class LeapAbility extends Ability {

    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
        double power = 0.5;
        if(arg.containsKey("power")) {
            power = Double.parseDouble(arg.get("power"));
        }
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            if((entity instanceof LivingEntity)) {
                if(!entity.getLocation().toVector().equals(self.getLocation().toVector())) {
                    self.setVelocity(self.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(power).multiply(-1));
                }
            }
        }
        return true;
    }
}
