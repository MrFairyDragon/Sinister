package com.AgarthaMC.Sinister.ThiefStuff;

import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalAvoidTarget;
import net.minecraft.world.entity.ai.goal.PathfinderGoalPanic;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomLookaround;
import net.minecraft.world.entity.ai.goal.PathfinderGoalRandomStrollLand;
import net.minecraft.world.entity.npc.EntityVillager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.World;

public class Thief extends EntityVillager {

    public final float Health = 20.0f;
    public static String thiefName = "Thief";

    public Thief(Location loc)
    {
        super(EntityTypes.aV, ((World) loc.getWorld()));

        AttributeInstance health = this.craftAttributes.getAttribute(Attribute.GENERIC_MAX_HEALTH); //Get a health instance for the villager

        health.setBaseValue(Health); //Set its max health
        this.setHealth(Health); //Set its current health

        this.setPosition(loc.getX(), loc.getY(), loc.getZ());
        this.setCustomName(new ChatComponentText(ChatColor.GOLD + thiefName));//Set its name
        this.setCustomNameVisible(true); //Set its name to visible

        this.bP.a(0, new PathfinderGoalAvoidTarget<EntityPlayer>(this, EntityPlayer.class, 15, 1.0D, 1.0D));
        this.bP.a(1, new PathfinderGoalPanic(this, 2.0D));
        this.bP.a(2, new PathfinderGoalRandomStrollLand(this, 0.6D));
        this.bP.a(3, new PathfinderGoalRandomLookaround(this));
        this.setVillagerData(this.getVillagerData().withProfession(VillagerProfession.l));
    }
}
