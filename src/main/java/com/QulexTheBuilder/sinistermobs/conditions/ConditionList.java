package com.QulexTheBuilder.sinistermobs.conditions;

import com.QulexTheBuilder.sinistermobs.abilities.Ability;
import org.bukkit.entity.Entity;

import java.util.List;
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
