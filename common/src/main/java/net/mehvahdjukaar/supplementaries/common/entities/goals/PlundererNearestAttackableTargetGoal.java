package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class PlundererNearestAttackableTargetGoal<A extends LivingEntity> extends NearestAttackableTargetGoal<A> {

    public PlundererNearestAttackableTargetGoal(PlundererEntity mob, Class<A> targetType, boolean mustSee) {
        super(mob, targetType, mustSee);
    }

    public PlundererNearestAttackableTargetGoal(PlundererEntity mob, Class<A> targetType, boolean mustSee, Predicate<LivingEntity> targetPredicate) {
        super(mob, targetType, mustSee, targetPredicate);
    }

    public PlundererNearestAttackableTargetGoal(PlundererEntity mob, Class<A> targetType, boolean mustSee, boolean mustReach) {
        super(mob, targetType, mustSee, mustReach);
    }

    public PlundererNearestAttackableTargetGoal(PlundererEntity mob, Class<A> targetType, int randomInterval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, targetType, randomInterval, mustSee, mustReach, targetPredicate);
    }

    @Override
    protected double getFollowDistance() {
        return Math.max(((PlundererEntity)mob).getSpyglassMaxSeeDistance(), super.getFollowDistance());
    }
}
