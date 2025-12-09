package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.mixins.EndermanAccessor;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class AbandonShipGoal extends Goal {
    private final Mob mob;
    private final int ticksToDismount;

    private int tickOnEdge = 0;

    public AbandonShipGoal(Mob mob, int ticksToDismount) {
        this.mob = mob;
        this.ticksToDismount = ticksToDismount;
    }

    public AbandonShipGoal(Mob mob) {
        this(mob, PlundererAICommon.TIME_TO_DISMOUNT_BOAT);
    }

    @Override
    public boolean canUse() {
        if (this.mob.getVehicle() instanceof Boat b) {
            LivingEntity captain = b.getControllingPassenger();
            if (captain != null && captain != this.mob && captain.getType().is(ModTags.CAN_STEER_BOAT)) {
                //my captain my captain
                return false;
            }
            if (!b.isInWater()) return true;
            if (b.onGround()) return true;
            PathNavigation nav = this.mob.getNavigation();
            //arrg, abandon ship!
            return nav.isStuck() || (!nav.isDone() && nav.getPath().getNextNode().type != PathType.WATER);
        }
        return false;
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
            doStopRiding();
        }
    }

    protected void doStopRiding() {
        Vec3 oldPos = this.mob.position();
        this.mob.stopRiding();
        if (this.mob instanceof EnderMan em) {
            em.setPos(oldPos);
            ((EndermanAccessor) em).invokeTeleport();
        }
    }
}