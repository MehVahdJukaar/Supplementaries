package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;

public class PlundererLookAtPlayerGoal extends LookAtPlayerGoal {
    public PlundererLookAtPlayerGoal(PlundererEntity mob, Class<? extends LivingEntity> lookAtType) {
        super(mob, lookAtType, mob.getSpyglassMaxSeeDistance());
    }

    public PlundererLookAtPlayerGoal(PlundererEntity mob, Class<? extends LivingEntity> lookAtType, float probability) {
        super(mob, lookAtType, mob.getSpyglassMaxSeeDistance(), probability);
    }

    public PlundererLookAtPlayerGoal(PlundererEntity mob, Class<? extends LivingEntity> lookAtType, float probability, boolean onlyHorizontal) {
        super(mob, lookAtType, mob.getSpyglassMaxSeeDistance(), probability, onlyHorizontal);
    }

}
