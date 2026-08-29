package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.mixins.EntityAccessor;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.vehicle.Boat;

import java.util.EnumSet;
import java.util.List;

public class BoardBoatGoal extends Goal {

    private static final int MAX_GOAL_TICKS = 20 * 20;
    private static final int REPATH_INTERVAL = 40;
    private static final double SEARCH_RADIUS = 8;

    private final Mob mob;
    private final double speedModifier;
    private final int tryInterval;
    // crew only hops on boats someone able to steer is already driving
    private final boolean needsCaptain;
    private int goalTick;
    private Boat boat;

    public BoardBoatGoal(Mob mob, double speedModifier, int tryInterval) {
        this(mob, speedModifier, tryInterval, false);
    }

    public BoardBoatGoal(Mob mob, double speedModifier, int tryInterval, boolean needsCaptain) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.tryInterval = tryInterval;
        this.needsCaptain = needsCaptain;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private Boat getNearestBoardableBoat() {
        List<? extends Boat> list = mob.level().getEntitiesOfClass(
                Boat.class, this.mob.getBoundingBox().inflate(SEARCH_RADIUS), this::isBoardable);

        Boat nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Boat boat : list) {
            double distance = this.mob.distanceToSqr(boat);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = boat;
            }
        }
        return nearest;
    }

    private boolean isBoardable(Boat boat) {
        if (needsCaptain) {
            LivingEntity captain = boat.getControllingPassenger();
            if (captain == null || !captain.getType().is(ModTags.CAN_STEER_BOAT)) return false;
        }
        return hasFreeSeat(boat);
    }

    private boolean hasFreeSeat(Boat boat) {
        if (boat instanceof EntityAccessor ea && this.mob instanceof EntityAccessor em) {
            return ea.invokeCanAddPassenger(this.mob) && em.invokeCanRide(boat);
        }
        return true;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getVehicle() != null) return false;
        if (needsCaptain && !this.mob.isInWater()) return false;
        LivingEntity target = this.mob.getTarget();
        boolean selfOrTargetInWater = this.mob.isInWater() || (target != null && target.isInWater());
        if (target != null && !selfOrTargetInWater) return false;
        if (this.mob.getRandom().nextInt(tryInterval) == 0) {
            this.boat = getNearestBoardableBoat();
            return boat != null;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.getVehicle() == null && this.goalTick < MAX_GOAL_TICKS
                && this.boat != null
                && this.boat.isAlive()
                && isBoardable(boat);
    }

    @Override
    public void start() {
        this.mob.getLookControl().setLookAt(this.boat);
        this.mob.getNavigation().moveTo(this.boat, this.speedModifier);
    }

    @Override
    public void stop() {
        this.boat = null;
        this.goalTick = 0;
    }

    @Override
    public void tick() {
        this.goalTick++;
        if (this.mob.closerThan(this.boat, this.boat.getBbWidth() / 2 + this.mob.getBbWidth() / 2)) {
            this.mob.startRiding(this.boat);
            this.boat = null;
        } else if (this.goalTick % REPATH_INTERVAL == 0) {
            this.mob.getNavigation().moveTo(this.boat, this.speedModifier);
        }
    }
}
