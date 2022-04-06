package com.QulexTheBuilder.sinistermobs;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

public class potionRainEvent implements Listener {


    public static boolean isPotionRaining = false;
    public static boolean doPotionRain = true;
    public static double range = 10;
    public static int power = 2;
    public static int duration = 20 * 10; //First number is to get it in seconds
    public static int intensity = 1;

    private static double height = 20;
    private static int time = 1200;
    private static int counter = 0;
    private static Random random = new Random();

    private static ItemStack getRandomPotion() {
        ItemStack item = new ItemStack(Material.SPLASH_POTION, 1);
        PotionMeta meta = ((PotionMeta) item.getItemMeta());
        PotionEffectType[] Pe = PotionEffectType.values();
        meta.addCustomEffect(new PotionEffect(Pe[random.nextInt(Pe.length)], duration, random.nextInt(power)), true);
        meta.setColor(Color.fromBGR(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
        item.setItemMeta(meta);
        return item;


    }

    private static void SpawnPotionOnAllPlayers(World world) {
        for(int i = 0; i < world.getPlayers().size(); i++) {
            Player player = world.getPlayers().get(i);
            Location location = player.getLocation();
            location.add(random.nextDouble() * range * 2 - (range), height, random.nextDouble() * range * 2 - (range));
            ItemStack item = getRandomPotion();
            ThrownPotion potion = (ThrownPotion) world.spawnEntity(location, EntityType.SPLASH_POTION);
            potion.setItem(item);
            potion.setVelocity(new Vector(0, -0.5, 0));
        }

    }

    public static void run() {
        if(isPotionRaining) {
            if(counter <= time) {
                World world = Bukkit.getServer().getWorld("world");
                for(int i = 0; i <= intensity; i++) {
                    SpawnPotionOnAllPlayers(world);
                }
                counter++;
            } else {
                isPotionRaining = false;
                counter = 0;
            }
        }
    }

    @EventHandler
    private void WeatherChangeEvent(WeatherChangeEvent event) {
        if(doPotionRain) {
            isPotionRaining = true;
            event.setCancelled(true);
        }
    }
}
