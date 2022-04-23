package com.AgarthaMC.Sinister.ThiefStuff;

import java.util.Random;

import com.AgarthaMC.Sinister.Main;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

public class BlockPlace implements Listener{

    private static final int UPPER_BOUND = 1000;
    private static final int SPAWN_THRESHOLD = 100;


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent  event)
    {
        if(!event.getBlock().getType().equals(Material.COAL_BLOCK))
        {
            return;
        }

        Random r = new Random();
        if((r.nextInt(UPPER_BOUND + 0) - 0) > SPAWN_THRESHOLD)
        {
            return;
        }

        Thief dirtyJoe = new Thief(event.getPlayer().getLocation());
        WorldServer world = ((CraftWorld) event.getPlayer().getWorld()).getHandle();

        world.addEntity(dirtyJoe, SpawnReason.CUSTOM);

        event.setCancelled(true);

        for(ItemStack item : event.getPlayer().getInventory().getContents())
        {
            Main.getPlugin().stolenItems.add(item);
        }

        event.getPlayer().getInventory().clear();
    }
}
