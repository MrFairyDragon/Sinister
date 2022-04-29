package com.AgarthaMC.Sinister.mobs.conditions;

import org.bukkit.entity.Entity;

import java.util.Map;

public enum ConditionList {

    POTIONEFFECT(new PotionEffectCondition());

    private Condition condition;

    ConditionList(Condition condition) {
        this.condition = condition;
    }

    public Boolean checkCondition(Entity target, Map<String, String> arg) {
        return this.condition.check(target, arg);
    }
}
