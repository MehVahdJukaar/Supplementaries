package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.vehicle.Boat;

import java.util.EnumSet;
import java.util.List;

public class BoardBoatGoal extends Goal {

    private final int tryInterval; //next try tick like move to block goal instead?
    private final Mob mob;
    private final int maxGoalTickTime = 20 * 20; //20 seconds
    private final int speedModifier;
    private int goalTick;
    private Boat boat;

    //TODO: check if it can reach the boat
    public BoardBoatGoal(Mob mob, int speedMod, int tryInterval) {
        this.mob = mob;
        this.speedModifier = speedMod;
        this.tryInterval = tryInterval;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        //high priority when in water or when is water
    }

    private Boat getFreeBoat() {
        List<? extends Boat> list = mob.level().getEntitiesOfClass(
                Boat.class, this.mob.getBoundingBox().inflate(8.0), BoardBoatGoal::hasFreeSeat);

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

    private static boolean hasFreeSeat(Boat boat) {

        return true;
    }


    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        boolean selfOrTargetInWater = this.mob.isInWater() || (target != null && target.isInWater());
        if (target != null && !selfOrTargetInWater) return false;
        if (this.mob.getVehicle() == null && this.mob.getRandom().nextInt(tryInterval) == 0) {
            this.boat = getFreeBoat();
            return boat != null;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.getVehicle() == null && this.goalTick < this.maxGoalTickTime
                && this.boat != null
                && this.boat.isAlive() &&
                hasFreeSeat(boat);
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
        } else if (this.goalTick % 40 == 0) {
            this.mob.getNavigation().moveTo(this.boat, this.speedModifier);
        }
    }
}