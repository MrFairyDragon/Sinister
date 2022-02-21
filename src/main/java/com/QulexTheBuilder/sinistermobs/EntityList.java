package com.QulexTheBuilder.sinistermobs;

import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class EntityList {

    public static Map<UUID, BossBar> bossMobs = new HashMap<>();
    public static Map<UUID, List<BukkitTask>> taskIDs = new HashMap<>();

}
