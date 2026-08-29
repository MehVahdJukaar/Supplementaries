package net.mehvahdjukaar.supplementaries.common.entities.controllers;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

public class BoatMoveController extends MoveControl {

    private static final float PLAYER_PADDLE_POWER = 0.04F;
    private static final float PLAYER_MOVEMENT_SPEED = 0.1F;
    // arbitrary slow down so mobs dont outrun players
    private static final float PADDLE_POWER_SCALE = 0.25F;
    private static final float MAX_TURN_PER_TICK = 45;
    private static final float ONE_PADDLE_TURN_ANGLE = 60;

    public BoatMoveController(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (!(this.mob.getControlledVehicle() instanceof Boat boat)) {
            super.tick();
            return;
        }
        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double dx = this.wantedX - boat.getX();
            double dz = this.wantedZ - boat.getZ();
            if (dx * dx + dz * dz < MIN_SPEED_SQR) {
                this.mob.setZza(0.0F);
                return;
            }
            float wantedYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
            float speed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            // PathNavigation stuck detection reads this
            this.mob.setSpeed(speed);
            this.mob.setZza(0.0F);
            driveBoat(boat, wantedYaw, speed);
        } else {
            // strafing and jumping mean nothing in a boat. Let it coast
            this.mob.setZza(0.0F);
        }
    }

    private void driveBoat(Boat boat, float wantedYaw, float speed) {
        float oldYaw = boat.getYRot();
        float newYaw = this.rotlerp(oldYaw, wantedYaw, MAX_TURN_PER_TICK);
        boat.setYRot(newYaw);
        // face the bow like a player would. Look control still turns the head
        this.mob.setYRot(newYaw);
        this.mob.setYBodyRot(newYaw);

        float power = speed / PLAYER_MOVEMENT_SPEED * PLAYER_PADDLE_POWER * PADDLE_POWER_SCALE;
        Vec3 push = new Vec3(
                Mth.sin(-newYaw * Mth.DEG_TO_RAD) * power,
                0.0,
                Mth.cos(newYaw * Mth.DEG_TO_RAD) * power);
        boat.setDeltaMovement(boat.getDeltaMovement().add(push));

        // same as Boat.controlBoat: both paddles when going forward, one when turning in place
        float turn = Mth.wrapDegrees(wantedYaw - oldYaw);
        boolean forward = Math.abs(turn) < ONE_PADDLE_TURN_ANGLE;
        boat.setPaddleState(forward || turn > 0, forward || turn < 0);
    }
}
