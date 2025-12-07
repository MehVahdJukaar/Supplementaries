package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonTrajectory;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonUtils;
import net.mehvahdjukaar.supplementaries.common.block.cannon.ShootingMode;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.ICannonShooter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.Vec3;

public final class UseCannonAICommon {

    public static final int MAX_STAY_TIME = 1000;
    public static final int MAX_TIME_WITHOUT_SHOOTING = 300;
    public static final int MAX_GO_TO_TIME = 1200;
    public static final int MIN_CANNON_RANGE = 16;
    public static final int GOAL_INTERVAL = 90;
    public static final int SHOOTING_COOLDOWN = 60;

    public static boolean aimCannonAndShoot(CannonAccess access, Mob shooter, LivingEntity target, boolean canShoot) {
        CannonBlockTile cannonTile = access.getInternalCannon();
        if (cannonTile.isOnCooldown()) return false;

        Vec3 cannonGlobalPosition = access.getCannonGlobalPosition(0);
        Vec3 targetLoc = target.position();

        //rough estimate of power needed
        byte power = 1;
        int maxPower = cannonTile.getFuel().getCount();
        float distance = (float) targetLoc.distanceTo(cannonGlobalPosition);

        if (distance > 64) {
            power = 4;
        } else if (distance > 32) {
            power = 3;
        } else if (distance > 16) {
            power = 2;
        }

        //hack. Aim bot
        //predict movement based off distance and speed
        targetLoc = targetLoc.add(target.getDeltaMovement().scale(distance * 0.2))
                .add(0, 0.6, 0);

        power = (byte) Math.min(power, maxPower);
        cannonTile.setPowerLevel(power);

        var comp = CannonUtils.computeTrajectory(access, targetLoc, ShootingMode.DOWN);

        var cannonTrajectory = comp.getFirst();
        float wantedGlobalYawDeg = comp.getSecond() * Mth.RAD_TO_DEG;
        if (cannonTrajectory != null) {
            float cannonGlobalYawOffsetDeg = access.getCannonGlobalYawOffset(0);
            float wantedLocalYawDeg = wantedGlobalYawDeg + cannonGlobalYawOffsetDeg;
            setCannonAnglesToFollowTrajectory(access, cannonTrajectory, wantedLocalYawDeg);

            if (canShoot) {
                float newCannonGlobalYaw = (cannonTile.getYaw() - cannonGlobalYawOffsetDeg) * Mth.DEG_TO_RAD;

                Vec3 hitLoc = cannonTrajectory.getHitLocation(cannonGlobalPosition, newCannonGlobalYaw);

                //distance
                double distance1 = hitLoc.distanceTo(targetLoc);
                if (distance1 < 0.1) {
                    cannonTile.ignite(shooter, access);

                    if (shooter instanceof ICannonShooter cs) {
                        cs.onShotCannon(cannonTile.getBlockPos());
                    } else if (shooter instanceof Raider r) {
                        r.playSound(r.getCelebrateSound(), 1.0F, 1.2F);
                    }
                    return true;
                }
            }
        }

        return false;


    }

    private static void setCannonAnglesToFollowTrajectory(CannonAccess access, CannonTrajectory trajectory,
                                                          float wantedLocalYawDeg) {
        if (trajectory != null) {
            float followSpeed = 1;
            CannonBlockTile cannon = access.getInternalCannon();
            //TODO: improve
            cannon.setPitch(access, Mth.rotLerp(followSpeed, cannon.getPitch(),
                    trajectory.pitch() * Mth.RAD_TO_DEG));
            // targetYawDeg = Mth.rotLerp(followSpeed, cannon.getYaw(0), targetYawDeg);
            cannon.setYaw(access, wantedLocalYawDeg);

            //sync
            cannon.setChanged();
            access.updateClients();
        }
    }

    public static boolean hasTargetInCannonRange(Mob mob) {
        var target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        double distSq = mob.distanceToSqr(target);
        return distSq > MIN_CANNON_RANGE * MIN_CANNON_RANGE;
    }


}
