package com.AgarthaMC.Sinister.conditions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Objects;

public class PotionEffectCondition extends Condition {

    @Override
    public Boolean check(Entity target, Map<String, String> arg) {
        if(target instanceof LivingEntity) {
            if(arg.containsKey("effect")) {
                if(((LivingEntity) target).hasPotionEffect(Objects.requireNonNull(PotionEffectType.getByName(arg.get("effect"))))) {
                    return true;
                }
            }
        }
        return false;
    }
}