package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class MeleeAttackGoalWhenInRange extends MeleeAttackGoal {

    public MeleeAttackGoalWhenInRange(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    public boolean canUse() {
        return isInFollowRange(this.mob, this.mob.getTarget()) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return isInFollowRange(this.mob, this.mob.getTarget()) &&  super.canContinueToUse();
    }

    public static boolean isInFollowRange(LivingEntity me, LivingEntity other){
        if (other == null) return false;
        if (!other.isAlive()) return false;
        double followDistance = me.getAttribute(Attributes.FOLLOW_RANGE).getValue();
        return me.distanceToSqr(other) <= followDistance * followDistance;
    }
}
