package com.AgarthaMC.Sinister;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MobEvents implements Listener {
    //event that calculates drops doesn't work with looting or luck.

    Random random = new Random();

    @EventHandler
    public void monsterDropEvent(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        YamlConfiguration yml = AbilityEvents.getFileByName(entity);
        if(yml == null) {
            return;
        }
        for(String str : yml.getStringList("drops")) {
            String[] strings = str.split(",");
            if(strings[0].equalsIgnoreCase("clear")) {
                event.getDrops().clear();
            } else {
                ItemStack item;
                if(Main.getPlugin().mobs.customItems.containsKey(strings[0].trim().toLowerCase())) {
                    item = new ItemStack(Main.getPlugin().mobs.customItems.get(strings[0].trim().toLowerCase()));
                } else {
                    item = new ItemStack(Material.valueOf(strings[0].trim().toUpperCase()), 1);
                }
                int min = 1;
                int max = 1;
                int chance = 100;
                if(strings.length >= 2) {
                    if(strings[1].contains("-")) {
                        min = Integer.parseInt(strings[1].trim().split("-")[0].trim());
                        max = Integer.parseInt(strings[1].trim().split("-")[1].trim());
                    } else {
                        min = Integer.parseInt(strings[1].trim());
                        max = min;
                    }
                }
                if(strings.length >= 3) {
                    chance = Integer.parseInt(strings[2].trim().split("%")[0].trim());
                }
                item = calculateDrop(item, min, max, chance);
                assert item != null;
                event.getDrops().add(item);
            }
        }
    }

    private ItemStack calculateDrop(ItemStack item, Integer amountMin, Integer amountMax, Integer chance) {
        item.setAmount(random.nextInt((amountMax+1-amountMin))+amountMin);
        if(random.nextInt(100)+1 <= chance) {
            return item;
        } else {
            return null;
        }
    }

    public static Location getAbilityLocation(Entity entity) {
        Location location = entity.getLocation();
        if(entity.getPersistentDataContainer().has(new NamespacedKey(Main.getPlugin(), "abilityPosition"), PersistentDataType.INTEGER_ARRAY)) {
            int[] abilityPositions = entity.getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "abilityPosition"), PersistentDataType.INTEGER_ARRAY);
            location = new Vector(abilityPositions[0], abilityPositions[1], abilityPositions[2]).toLocation(entity.getWorld());
        }
        return location;
    }

    @EventHandler
    private void onPlayerMoveEvent(PlayerMoveEvent event) {
        List<UUID> mobId = new ArrayList<>();

        //Get nearby entities is a very expansive method
        for(Entity entity : event.getPlayer().getNearbyEntities(30,30,30)) {
            mobId.add(entity.getUniqueId());
            if(EntityList.bossMobs.containsKey(entity.getUniqueId())) {
                BossBar bb = EntityList.bossMobs.get(entity.getUniqueId());
                bb.addPlayer(event.getPlayer());
            }
        }
        for(UUID id : EntityList.bossMobs.keySet()) {
            if(!(mobId.contains(id))) {
                BossBar bb = EntityList.bossMobs.get(id);
                bb.removePlayer(event.getPlayer());
            }
        }
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        if(EntityList.taskIDs.containsKey(event.getEntity().getUniqueId())) {
            List<BukkitTask> tasks = EntityList.taskIDs.get(event.getEntity().getUniqueId());
            for(BukkitTask task : tasks) {
                task.cancel();
            }
        }
        if(EntityList.bossMobs.containsKey(event.getEntity().getUniqueId())) {
            BossBar bb = EntityList.bossMobs.get(event.getEntity().getUniqueId());
            EntityList.bossMobs.remove(event.getEntity().getUniqueId());
            bb.removeAll();
        }
        AbilityEvents.repeatMap.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    private void onEntityHurt(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) event.getEntity();
        if(EntityList.bossMobs.containsKey(entity.getUniqueId())) {
            BossBar bb = EntityList.bossMobs.get(entity.getUniqueId());
            AttributeInstance healthInstance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double healthMax = healthInstance.getBaseValue();
            double health = entity.getHealth();
            bb.setProgress(health/healthMax);
        }
    }

    @EventHandler
    private void onFallDamage(EntityDamageEvent event) {
        if(event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            if(event.getEntity().getPersistentDataContainer().has(new NamespacedKey(Main.getPlugin(), "nofall"), PersistentDataType.STRING)) {
                if(event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "nofall"), PersistentDataType.STRING).trim().equalsIgnoreCase("true")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void onShootProjectile(EntityShootBowEvent event) {
        if(event.getEntity().getPersistentDataContainer().has(new NamespacedKey(Main.getPlugin(), "arrowColor"), PersistentDataType.STRING)) {
            String arrowColor = event.getEntity().getPersistentDataContainer().get(new NamespacedKey(Main.getPlugin(), "arrowColor"), PersistentDataType.STRING);
            Entity arrow = event.getProjectile();
            if(arrow instanceof Arrow) {
                ((Arrow) arrow).setColor(Color.fromRGB(Integer.parseInt(arrowColor, 16)));
            }
            event.setProjectile(arrow);
        }
    }

    @EventHandler
    private void reduceHorseDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Horse) {
            Horse horse = (Horse) event.getEntity();
            if(horse.getPassengers().get(0) == null) {
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
