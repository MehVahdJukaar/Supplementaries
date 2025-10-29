package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.vehicle.Boat;
import org.jetbrains.annotations.Nullable;

public class RideBoatTowardTargetGoal extends MoveTowardsTargetGoal {
    private final PathfinderMob mob;

    public RideBoatTowardTargetGoal(PathfinderMob mob, double speedModifier, float within) {
        super(mob, speedModifier, within);
        this.mob = mob;
    }

    @Override
    public void tick() {
        super.tick();
        Boat boat = getBoat();
        boat.setInput(mob.xxa < 0, mob.xxa > 0, mob.zza > 0, mob.zza < 0);
        boat.controlBoat();

    }

    @Override
    public boolean canUse() {
        return super.canUse() && getBoat() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && getBoat() != null;
    }

    @Nullable
    private Boat getBoat() {
        Entity vehicle = this.mob.getVehicle();
        if (vehicle instanceof Boat b) {
            return b;
        }
        return null;
    }


}
