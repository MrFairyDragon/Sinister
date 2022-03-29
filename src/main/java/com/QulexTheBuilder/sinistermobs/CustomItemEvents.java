package com.QulexTheBuilder.sinistermobs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CustomItemEvents implements Listener {

    Map<ItemStack, BukkitTask> runningBukkitTasks;

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent event) {
        Main.getPlugin().mobs.customItems.forEach((key, value) -> {
            for(ItemStack item : event.getInventory().getContents()) {
                if(item.equals(value)) {
                    System.out.println("I'm calling runnable");
                    startAnimationRunnable(item, key);
                }
            }
        });
    }

    private void startAnimationRunnable(ItemStack item, String name) {
        runningBukkitTasks.forEach((key, value) -> {
            if(!(key == item)) {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(new File("Items." + name));
                final List<Integer> animationFrames = yml.getIntegerList("Animation");
                final Integer[] interation = {0};

                BukkitTask task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        ItemMeta meta = item.getItemMeta();
                        assert meta != null;
                        meta.setCustomModelData(animationFrames.get(interation[0]));
                        System.out.println(animationFrames.get(interation[0]));
                        item.setItemMeta(meta);
                        interation[0]++;
                        if(interation[0] > animationFrames.size()) {
                            interation[0] = 0;
                        }
                    }
                }.runTaskTimer(Main.getPlugin(), 0, yml.getInt("AnimationSpeed"));
                runningBukkitTasks.put(item, task);
            }
        });
    }
}
