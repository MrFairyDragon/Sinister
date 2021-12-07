package com.QulexTheBuilder.sinistermobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.Objects;

public class Conditions {

    public Boolean hasEffectCondition(Map<String, String> parameterMap, Entity target) {
        if(target instanceof LivingEntity) {
            if(parameterMap.containsKey("effect")) {
                if(((LivingEntity) target).hasPotionEffect(Objects.requireNonNull(PotionEffectType.getByName(parameterMap.get("effect"))))) {
                    return true;
                }
            }
        }
        return false;
    }
}
