package com.AgarthaMC.Sinister.thiefStuff;

import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalAvoidTarget;
import net.minecraft.world.entity.ai.goal.PathfinderGoalPanic;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStrollLand;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomLookaround;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;

public class Thief extends EntityVillager {

    public final float Health = 20.0f;

    public static String thiefName = "Thief";

    public Thief(Location loc) {
        super(EntityTypes.aV, ((CraftWorld) loc.getWorld()).getHandle());

        AttributeInstance health = this.craftAttributes.getAttribute(Attribute.GENERIC_MAX_HEALTH); //Get a health instance for the villager

        health.setBaseValue(Health);
        this.a(Health);

        this.b(loc.getX(), loc.getY(), loc.getZ());

        this.a(new ChatComponentText(ChatColor.GOLD + thiefName)); //Set its custom name
        this.n(true);

        // The thief will move away from the player, panic if the player is nearby, stroll if the player isn't around, and look around if standing still

        this.bQ.a(0, new PathfinderGoalAvoidTarget<EntityPlayer>(this, EntityPlayer.class, 15, 1.0D, 1.0D));
        this.bQ.a(1, new PathfinderGoalPanic(this, 2.0D));
        this.bQ.a(2, new PathfinderGoalRandomStrollLand(this, 0.6D));
        this.bQ.a(3, new PathfinderGoalRandomLookaround(this));

        this.a(new VillagerData(this.fK().a(), VillagerProfession.l, 0)); //Set the profession to NITWIT
    }
}