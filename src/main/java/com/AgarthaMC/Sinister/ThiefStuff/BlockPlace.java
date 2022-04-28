package com.AgarthaMC.Sinister.ThiefStuff;

import java.util.Random;

import com.AgarthaMC.Sinister.Main;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
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
        World world = ((CraftWorld) event.getPlayer().getWorld()).getHandle();

        world.addFreshEntity(dirtyJoe, SpawnReason.CUSTOM);

        event.setCancelled(true);

        for(ItemStack item : event.getPlayer().getInventory().getContents())
        {
            Main.getPlugin().stolenItems.add(item);
        }

        event.getPlayer().getInventory().clear();
    }
}
