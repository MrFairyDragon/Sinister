package com.QulexTheBuilder.sinistermobs.abilities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApplyEffectAbility extends Ability {

    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
            for(Entity entity : targets) {
                if(entity == null) {
                    return false;
                }
                if(!(entity instanceof LivingEntity)) {
                    return false;
                }
                try {
                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(arg.get("effect").toUpperCase())), Integer.parseInt(arg.get("duration")), Integer.parseInt(arg.get("amplifier")), true, true));
                } catch(Exception e) {
                    System.out.println("[Error] No such PotionEffect");
                    return false;
                }
            }
        return true;
    }
}
