package com.QulexTheBuilder.sinistermobs.conditions;

import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;

public abstract class Condition {

    public abstract Boolean check(Entity target, Map<String, String> arg);

}
