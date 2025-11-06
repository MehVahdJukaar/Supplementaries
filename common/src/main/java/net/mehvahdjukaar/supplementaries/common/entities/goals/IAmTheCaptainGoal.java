package net.mehvahdjukaar.supplementaries.common.entities.goals;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.vehicle.Boat;

public class IAmTheCaptainGoal extends Goal {
    private final Mob mob;

    public IAmTheCaptainGoal(Mob mob) {
        this.mob = mob;
        Preconditions.checkArgument(mob.getType().is(ModTags.CAN_STEER_BOAT));
    }

    @Override
    public boolean canUse() {
        if (this.mob.getVehicle() instanceof Boat b) {
            LivingEntity captain = b.getControllingPassenger();
            if (captain == null || captain == this.mob) return false;
            if (captain.getType().is(ModTags.CAN_STEER_BOAT)) {
                return false;
            }

        }
        return false;
    }

    @Override
    public void start() {
        if (this.mob.getVehicle() instanceof Boat b) {
            var allPassengers = b.getPassengers();
            b.ejectPassengers();
            this.mob.startRiding(b, true);
            for (var other : allPassengers) {
                if (other != this.mob) {
                    other.startRiding(b, true);
                }
            }
        }
    }

}