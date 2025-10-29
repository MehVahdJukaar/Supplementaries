package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;

public class BoatAwareMoveController extends MoveControl {


    public BoatAwareMoveController(Mob mob) {
        super(mob);

    }

    private boolean isWalkableWithBoat(float relativeX, float relativeZ) {
        PathNavigation pathNavigation = this.mob.getNavigation();
        if (pathNavigation != null) {
            NodeEvaluator nodeEvaluator = pathNavigation.getNodeEvaluator();
            if (nodeEvaluator != null
                    && nodeEvaluator.getPathType(this.mob, BlockPos.containing(this.mob.getX() + relativeX, this.mob.getBlockY(), this.mob.getZ() + relativeZ))
                    != PathType.WALKABLE) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void tick() {
        Entity vehicle = this.mob.getVehicle();
        if (!(vehicle instanceof Boat boat)) {
            super.tick();
            return;
        }
        if (this.operation == MoveControl.Operation.STRAFE) {
            float moveSpeedMod = (float) this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
            float actualSpeed = (float) this.speedModifier * moveSpeedMod;
            float forward = this.strafeForwards;
            float right = this.strafeRight;
            float len = Mth.sqrt(forward * forward + right * right);
            if (len < 1.0F) {
                len = 1.0F;
            }

            len = actualSpeed / len;
            forward *= len;
            right *= len;
            float sin = Mth.sin(this.mob.getYRot() * (float) (Math.PI / 180.0));
            float cos = Mth.cos(this.mob.getYRot() * (float) (Math.PI / 180.0));
            float x = forward * cos - right * sin;
            float z = right * cos + forward * sin;
            if (!this.isWalkableWithBoat(x, z)) {
                this.strafeForwards = 1.0F;
                this.strafeRight = 0.0F;
            }

            this.mob.setSpeed(actualSpeed);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
            this.operation = MoveControl.Operation.WAIT;
        } else if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;

            double distX = this.wantedX - this.mob.getX();
            double distZ = this.wantedZ - this.mob.getZ();
            double distY = this.wantedY - this.mob.getY();
            double lenSq = distX * distX + distY * distY + distZ * distZ;
            if (lenSq < 2.5000003E-7F) {
                this.mob.setZza(0.0F);
                return;
            }

            float angle = (float) (Mth.atan2(distZ, distX) * Mth.RAD_TO_DEG) - 90.0F;
            //this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));

            controlBoat(boat, angle);

        } else {
            this.mob.setZza(0.0F);
        }
    }

    private void controlBoat(Boat boat, float wantedAngle) {
        if (!boat.isVehicle()) return;
        float amount = 0.0F;

        /*
        if (boat.inputLeft) {
            boat.deltaRotation--;
        }

        if (boat.inputRight) {
            boat.deltaRotation++;
        }

        //when just input right or input left
        if (boat.inputRight != boat.inputLeft && !boat.inputUp && !boat.inputDown) {
            amount += 0.005F;
        }*/

        boat.setYRot(this.rotlerp(boat.getYRot(), wantedAngle, 90.0F));
        // boat.setYRot(boat.getYRot() + boat.deltaRotation);
        if (true) {
            amount += 0.04F;
        }

        //  if (boat.inputDown) {
        //     amount -= 0.005F;
        // }

        boat.setDeltaMovement(
                boat.getDeltaMovement().add(
                        Mth.sin(-boat.getYRot() * Mth.DEG_TO_RAD) * amount,
                        0.0,
                        Mth.cos(boat.getYRot() * Mth.DEG_TO_RAD) * amount)
        );

        //TODO: allow them to rotate only too with look controller
        boat.setPaddleState(true, true);
    }

}
