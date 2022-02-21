package com.QulexTheBuilder.sinistermobs.abilities;

import com.QulexTheBuilder.sinistermobs.MobEvents;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.Map;

public class TransformBlockAbility extends Ability{
    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
        Material toBlock = Material.AIR;
        Material fromBlock = Material.AIR;
        if(arg.containsKey("toBlock")) {
            toBlock = Material.valueOf(arg.get("toBlock").toUpperCase());
        }
        if(arg.containsKey("fromBlock")) {
            fromBlock = Material.valueOf(arg.get("fromBlock").toUpperCase());
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
            if(location.getBlock().getRelative(0,-1,0).getType() == fromBlock) {
                location.getBlock().getRelative(0, -1, 0).setType(toBlock);
            }
        }
        return true;
    }
}
