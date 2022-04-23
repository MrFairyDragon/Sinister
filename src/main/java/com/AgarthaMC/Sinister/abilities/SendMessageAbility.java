package com.AgarthaMC.Sinister.abilities;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class SendMessageAbility extends Ability {
    @Override
    public Boolean start(Entity self, List<Entity> targets, Map<String, String> arg) {
        String color = "WHITE";
        if(!arg.containsKey("message")) {
            System.out.println("No message found");
            return false;
        }
        if(arg.containsKey("color")) {
            color = arg.get("color");
        }
        for(Entity entity : targets) {
            if(entity == null) {
                System.out.println("Found no target?");
                return false;
            }
            if(entity instanceof Player) {
                entity.sendMessage(ChatColor.valueOf(color) + arg.get("message"));
            }
        }
        return true;
    }
}
