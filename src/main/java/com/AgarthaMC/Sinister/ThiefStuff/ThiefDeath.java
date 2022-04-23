package com.AgarthaMC.Sinister.ThiefStuff;

import java.util.Random;

import com.AgarthaMC.Sinister.Main;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class ThiefDeath implements Listener{

    private ItemStack[] goldsack =
            {
                    new ItemStack(Material.GOLD_BLOCK, 64),
                    new ItemStack(Material.GOLD_INGOT, 64)
            };

    @EventHandler
    public void onDeath(EntityDeathEvent event)
    {
        if(!(event.getEntity() instanceof Villager))
        {
            return;
        }
        if(event.getEntity().getCustomName() == null)
        {
            return;
        }
        if(!event.getEntity().getCustomName().contains("Thief"))
        {
            return;
        }

        Random r = new Random();

        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), goldsack[r.nextInt(goldsack.length + 0) - 0]); //Drop items that the thief has

        if(Main.getPlugin() != null)
        {
            for(ItemStack item : Main.getPlugin().stolenItems)
            {
                if(item != null)
                {
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), item); //Drop the items the thief stole from the player
                }
            }
        }
    }
}
