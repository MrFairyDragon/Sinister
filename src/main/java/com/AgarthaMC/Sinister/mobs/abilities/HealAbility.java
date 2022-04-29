package com.AgarthaMC.Sinister.mobs.abilities;

import com.AgarthaMC.Sinister.mobs.EntityList;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Map;

public class HealAbility extends Ability {
    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
        double health = 0;
        if(arg.containsKey("health")) {
            health = Double.parseDouble(arg.get("health"));
        }
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            if(!(entity instanceof LivingEntity)) {
                return false;
            }
            LivingEntity livingEntity = (LivingEntity) entity;
            AttributeInstance healthInstance = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if(livingEntity.getHealth() + health >= healthInstance.getBaseValue()) {
                livingEntity.setHealth(healthInstance.getBaseValue());
            }
            ((LivingEntity) entity).setHealth(livingEntity.getHealth()+health);
            if(EntityList.bossMobs.containsKey(entity.getUniqueId())) {
                BossBar bb = EntityList.bossMobs.get(entity.getUniqueId());
                bb.setProgress(livingEntity.getHealth()/healthInstance.getBaseValue());
            }
        }
        return true;
    }
}
