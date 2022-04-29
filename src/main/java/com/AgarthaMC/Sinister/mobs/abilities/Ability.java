package com.AgarthaMC.Sinister.mobs.abilities;

import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Ability {

    protected final Random random = new Random();

    public abstract Boolean start(Entity self, List<Entity> targets, Map<String, String> arg);
}
