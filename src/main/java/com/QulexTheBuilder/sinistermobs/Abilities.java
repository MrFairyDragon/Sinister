package com.QulexTheBuilder.sinistermobs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class Abilities {

    //This is all the ability's they should use all the variable generated from the above even if it doesn't make sense,
    //like a boss striking itself with lightning

    private Random random = new Random();

    public Boolean spawnMobAbility(Map<String, String> arg, List<Entity> targets, Boolean healthCheck, Integer chance, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            boolean success = false;
            for(File file : Objects.requireNonNull(InstantiateMobs.filesInDirectory("mobs"))) {
                if(file.getName().equalsIgnoreCase(arg.get("type") + ".yml")) {
                    Main.getPlugin().mobs.spawnMob(arg.get("type"), entity.getLocation());
                    success = true;
                }
            }
            if(!success) {
                try {
                    Objects.requireNonNull(entity.getLocation().getWorld()).spawnEntity(entity.getLocation(), EntityType.valueOf(arg.get("type").toUpperCase()));
                } catch(Exception e) {
                    System.out.println(ChatColor.RED+"[Error] There is no custom or vanilla with this name: " + ChatColor.WHITE+arg);
                    return false;
                }
            }
        }
        return true;
    }
    public Location getSafeLocation(Location location, Integer distance) {
        for(int i = 0; i < 20; i++) {
            Location newLocation = location.clone().add(random.nextInt(distance) - distance / 2, random.nextInt(distance) - distance / 2, random.nextInt(distance) - distance / 2);
            if(newLocation.getBlock().isEmpty() && newLocation.clone().add(0, 1, 0).getBlock().isEmpty() && !newLocation.clone().add(0, -1, 0).getBlock().isEmpty()) {
                return newLocation;
            }
        }
        return location;
    }

    public Boolean spawnMountAbility(Map<String, String> arg, List<Entity> targets, Boolean healthCheck, Integer chance, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            if(entity.isInsideVehicle()) {
                return false;
            }
            boolean success = false;
            for(File file : Objects.requireNonNull(InstantiateMobs.filesInDirectory("mobs"))) {
                if(file.getName().equalsIgnoreCase(arg.get("type") + ".yml")) {
                    YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                    Entity spawnedEntity = Objects.requireNonNull(entity.getLocation().getWorld()).spawnEntity(entity.getLocation(), EntityType.valueOf(yml.getString("type").toUpperCase()));
                    spawnedEntity.addPassenger(entity);
                    Main.getPlugin().mobs.changeMob(arg.get("type"), spawnedEntity);
                    success = true;
                }
            }
            if(!success) {
                try {
                    Entity spawnedEntity = Objects.requireNonNull(entity.getLocation().getWorld()).spawnEntity(entity.getLocation(), EntityType.valueOf(arg.get("type").toUpperCase()));
                    spawnedEntity.addPassenger(entity);
                } catch(Exception e) {
                    System.out.println(ChatColor.RED + "[Error] There is no custom or vanilla with this name: " + ChatColor.WHITE + arg);
                    return false;
                }
            }
        }
        return true;
    }

    public Boolean spawnBlockAbility(Map<String, String> arg, List<Entity> targets, Boolean healthCheck, Integer chance, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            Block block = entity.getLocation().getWorld().getBlockAt(entity.getLocation().getBlockX(), entity.getLocation().getBlockY(), entity.getLocation().getBlockZ());
            try {
                block.setType(Material.valueOf(arg.get("type").toUpperCase()));
            } catch(Exception e) {
                System.out.println("There is no such block");
                return false;
            }
        }
        return true;
    }

    public Boolean applyEffectAbility(Map<String, String> arg, List<Entity> targets, Boolean healthCheck, Integer chance, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
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
                System.out.println("No such PotionEffect");
                return false;
            }
        }
        return true;
    }

    public Boolean pushAbility(Map<String, String> arg, List<Entity> targets, Boolean healthCheck, Integer chance, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
        double power = 0.5;
        if(arg.containsKey("health")) {
            power = Double.parseDouble(arg.get("health"));
        }
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            if(!(entity instanceof LivingEntity)) {
                return false;
            }
            entity.setVelocity(entity.getLocation().toVector().subtract(self.getLocation().toVector()).normalize().multiply(power));
        }
        return true;
    }

    public Boolean pullAbility(Map<String, String> arg, List<Entity> targets, Boolean healthCheck, Integer chance, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
        double power = 0.5;
        if(arg.containsKey("health")) {
            power = Double.parseDouble(arg.get("health"));
        }
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            if(!(entity instanceof LivingEntity)) {
                return false;
            }
            entity.setVelocity(entity.getLocation().toVector().subtract(self.getLocation().toVector()).normalize().multiply(power).multiply(-1));
        }
        return true;
    }

    public Boolean leapAbility(Map<String, String> arg, List<Entity> targets, Boolean healthCheck, Integer chance, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
        double power = 0.5;
        if(arg.containsKey("health")) {
            power = Double.parseDouble(arg.get("health"));
        }
        for(Entity entity : targets) {
            if(entity == null) {
                return false;
            }
            if(!(entity instanceof LivingEntity)) {
                return false;
            }
            self.setVelocity(self.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(power).multiply(-1));
        }
        return true;
    }

    public Boolean healAbility(Map<String, String> arg, List<Entity> targets, Boolean healthCheck, Integer chance, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
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
                return false;
            }
            ((LivingEntity) entity).setHealth(livingEntity.getHealth()+health);
            if(Main.getPlugin().mobs.bossMobs.containsKey(entity.getUniqueId())) {
                BossBar bb = Main.getPlugin().mobs.bossMobs.get(entity.getUniqueId());
                bb.setProgress(livingEntity.getHealth()/healthInstance.getBaseValue());
            }
        }
        return true;
    }

    public Boolean transformBlockAbility(Map<String, String> arg, List<Entity> targets, Boolean healthCheck, Integer chance, Entity self) {
        if(!healthCheck) {
            return false;
        }
        if(random.nextInt(100) >= chance) {
            return false;
        }
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
            if(entity.getLocation().getBlock().getRelative(0,-1,0).getType() == fromBlock) {
                entity.getLocation().getBlock().getRelative(0, -1, 0).setType(toBlock);
            }
        }
        return true;
    }
}
