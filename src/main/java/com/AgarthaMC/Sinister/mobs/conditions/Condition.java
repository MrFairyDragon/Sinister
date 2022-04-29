package com.AgarthaMC.Sinister.mobs.conditions;

import org.bukkit.entity.Entity;

import java.util.Map;

public abstract class Condition {

    public abstract Boolean check(Entity target, Map<String, String> arg);

}
