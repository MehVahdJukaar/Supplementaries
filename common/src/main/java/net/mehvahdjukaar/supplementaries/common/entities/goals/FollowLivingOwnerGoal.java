package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.entities.data.LivingEntityTamable;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

//Like FollowOwnerGoal but only follows living owner owners and has teleporting tweaks
public class FollowLivingOwnerGoal extends Goal {
    private final Mob mob;
    @Nullable
    public LivingEntity owner;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;

    public FollowLivingOwnerGoal(Mob mob, double speedModifier, float startDistance, float stopDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.navigation = mob.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(mob.getNavigation() instanceof GroundPathNavigation) && !(mob.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canUse() {
        LivingEntityTamable livingTamable = ModEntities.LIVING_TAMABLE.getOrCreate(this.mob);
        LivingEntity owner = livingTamable.getOwner(this.mob);
        if (owner == null) {
            return false;
        } else if (livingTamable.unableToMoveToOwner(this.mob)) {
            return false;
        } else if (this.mob.distanceToSqr(owner) < (double) (this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = owner;
            return true;
        }
    }


    @Override
    public boolean canContinueToUse() {
        LivingEntityTamable livingTamable = ModEntities.LIVING_TAMABLE.getOrCreate(this.mob);
        if (this.navigation.isDone()) {
            return false;
        } else if (livingTamable.unableToMoveToOwner(this.mob)) {
            return false;
        } else {
            return !(this.mob.distanceToSqr(this.owner) <= (double) (this.stopDistance * this.stopDistance));
        }
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER);
        this.mob.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mob.getMaxHeadXRot());

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            this.navigation.moveTo(this.owner, this.speedModifier);
        }
    }
}

