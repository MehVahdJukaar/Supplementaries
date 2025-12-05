package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.mehvahdjukaar.supplementaries.common.entities.data.LivingEntityTamable;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;

public class ParrotLandOnPlundererGoal extends Goal {
    private final ShoulderRidingEntity entity;
    private PlundererEntity plunderer;
    private boolean isSittingOnShoulder;

    public ParrotLandOnPlundererGoal(ShoulderRidingEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        //get owner just returns players, for not
        LivingEntityTamable tamable = ModEntities.LIVING_TAMABLE.getOrCreate(this.entity);
        if (!(tamable.getOwner(this.entity) instanceof PlundererEntity pl)) {
            return false;
        }
        this.plunderer = pl;
        boolean valid = !pl.isUnderWater() && !pl.isInPowderSnow && pl.isAlive() && !pl.isSleeping() && !pl.isAggressive();
        return !this.entity.isOrderedToSit() && valid && this.entity.canSitOnShoulder();
    }

    //if ones with higher priority can interrupt it
    @Override
    public boolean isInterruptable() {
        return !this.isSittingOnShoulder;
    }

    @Override
    public void start() {
        this.isSittingOnShoulder = false;
    }

    @Override
    public void tick() {
        if (!this.isSittingOnShoulder && !this.entity.isInSittingPose() && !this.entity.isLeashed()) {
            double cheatRadius = 0.25;
            if (this.entity.getBoundingBox().inflate(cheatRadius, cheatRadius, cheatRadius)
                    .intersects(this.plunderer.getBoundingBox())) {
                this.isSittingOnShoulder = this.plunderer.setEntityOnShoulder(this.entity);
            }
        }
    }
}

