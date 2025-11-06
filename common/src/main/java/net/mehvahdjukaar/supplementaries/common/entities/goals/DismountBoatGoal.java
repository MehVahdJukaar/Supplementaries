package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.pathfinder.PathType;

public class DismountBoatGoal extends Goal {
    private final Mob mob;
    private final int ticksToDismount;

    private int tickOnEdge = 0;

    public DismountBoatGoal(Mob mob, int ticksToDismount) {
        this.mob = mob;
        this.ticksToDismount = ticksToDismount;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getVehicle() instanceof Boat) {
            boolean isOnEdge = isOnEdge();
            return isOnEdge;
        }
        return false;
    }

    private boolean isOnEdge() {
        PathNavigation nav = this.mob.getNavigation();
        boolean isOnEdge = nav.isStuck() || (!nav.isDone() && nav.getPath()
                .getNextNode().type != PathType.WATER) || !this.mob.getVehicle().isInWater();
        return isOnEdge;
    }

    @Override
    public void start() {
        this.tickOnEdge = 0;
    }

    @Override
    public void stop() {
        this.tickOnEdge = 0;
    }

    @Override
    public void tick() {
        if (tickOnEdge++ >= ticksToDismount) {
            this.mob.stopRiding();
        }
    }
}