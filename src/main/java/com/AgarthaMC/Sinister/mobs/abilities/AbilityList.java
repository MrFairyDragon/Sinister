package com.AgarthaMC.Sinister.mobs.abilities;

import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;

public enum AbilityList {

    APPLYEFFECT(new ApplyEffectAbility()),
    APPLYDAMAGE(new ApplyDamageAbility()),
    HEAL(new HealAbility()),
    LEAP(new LeapAbility()),
    PULL(new PullAbility()),
    PUSH(new PushAbility()),
    RANDOMLOCATION(new RandomLocationAbility()),
    SENDMESSAGE(new SendMessageAbility()),
    SPAWNBLOCK(new SpawnBlockAbility()),
    SPAWNMOB(new SpawnMobAbility()),
    SPAWNMOUNT(new SpawnMountAbility()),
    SPAWNPARTICLE(new SpawnParticleAtLocationAbility()),
    SPAWNPARTICLELINE(new SpawnParticleLineAbility()),
    SPAWNPARTICLESPHERE(new SpawnParticleSphereAbility()),
    SURFACELOCATION(new SurfaceLocationAbility()),
    TRANSFORMBLOCK(new TransformBlockAbility());
    private Ability ability;

    AbilityList(Ability ability) {
        this.ability = ability;
    }
    public Boolean doAbility(Entity self, List<Entity> targets, Map<String, String> arg) {
        return this.ability.start(self, targets, arg);
    }
}
