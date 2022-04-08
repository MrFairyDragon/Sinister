package com.QulexTheBuilder.sinistermobs;

import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class HorseEvents implements Listener {

    @EventHandler
    private void reduceHorseDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Horse) {
            Horse horse = (Horse) event.getEntity();
            if(horse.getPassengers() == null) {
                event.setDamage(event.getDamage() / 4);
            }
            if(horse.getPassengers().get(0) instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) horse.getPassengers().get(0);
                livingEntity.damage(event.getDamage() * 0.9);
                event.setCancelled(true);
            } else {
                event.setDamage(event.getDamage() / 4);
            }
        }
    }
}
