package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.block.cannon.BallisticTrajectory3D;
import net.mehvahdjukaar.supplementaries.common.block.cannon.BallisticTrajectory;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonUtils;
import net.mehvahdjukaar.supplementaries.common.block.cannon.ShootingMode;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.ICannonShooter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.Vec3;

public final class PlundererAICommon {

    public static final int TIME_TO_DISMOUNT_BOAT = 15;

    public static final int MAX_USE_CANNON_BOAT = 20 * 15;

    public static final int MAX_STAY_TIME = 1000;
    public static final int MAX_TIME_WITHOUT_SHOOTING = 300;
    public static final int MAX_GO_TO_TIME = 1200;
    public static final int MIN_CANNON_RANGE = 16;
    public static final int GOAL_INTERVAL = 90;
    public static final int SHOOTING_COOLDOWN_MIN = 50;
    public static final int SHOOTING_COOLDOWN_MAX = 80;

    public static boolean aimCannonAndShoot(CannonBlockTile tile, Mob shooter, LivingEntity target, boolean canShoot) {
        if (tile.isOnCooldown()) return false;

        Vec3 cannonGlobalPosition = tile.getGlobalPosition(0);
        Vec3 targetLoc = target.position();

        //rough estimate of power needed
        byte power = 1;
        int maxPower = tile.getFuel().getCount();
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
        tile.setPowerLevel(power);

        BallisticTrajectory3D comp = CannonUtils.computeTrajectory(tile, targetLoc, ShootingMode.DOWN);

        var cannonTrajectory = comp.trajectory();
        float wantedGlobalYawDeg = comp.yaw() * Mth.RAD_TO_DEG;
        if (cannonTrajectory != null) {
            float cannonGlobalYawOffsetDeg = 0;
            float wantedLocalYawDeg = wantedGlobalYawDeg + cannonGlobalYawOffsetDeg;
            setCannonAnglesToFollowTrajectory(tile, cannonTrajectory, wantedLocalYawDeg);

            if (canShoot) {
                //TODO cannon change
                float cannonYawTempCHange = 0; //tile.getYaw()
                float newCannonGlobalYaw = (cannonYawTempCHange - cannonGlobalYawOffsetDeg) * Mth.DEG_TO_RAD;

                Vec3 hitLoc = cannonTrajectory.getHitLocation(cannonGlobalPosition, newCannonGlobalYaw);
                //distance
                double distance1 = hitLoc.distanceTo(targetLoc);
                if (distance1 < 0.1 && tile.readyToFire()) {
                    tile.ignite(shooter);

                    if (shooter instanceof ICannonShooter cs) {
                        cs.onShotCannon(tile.getBlockPos());
                    } else if (shooter instanceof Raider r) {
                        r.playSound(r.getCelebrateSound(), 2.5F, 1F);
                    }
                    return true;
                }
            }
        }

        return false;


    }

    private static void setCannonAnglesToFollowTrajectory(CannonBlockTile tile, BallisticTrajectory trajectory,
                                                          float wantedLocalYawDeg) {
        if (trajectory != null) {
            float followSpeed = 1;
            //TODO: improve
            //TODO: cannon change
           // tile.setPitch(Mth.rotLerp(followSpeed, tile.getPitch(),
             //       trajectory.pitch() * Mth.RAD_TO_DEG));
            // targetYawDeg = Mth.rotLerp(followSpeed, cannon.getYaw(0), targetYawDeg);
            //tile.setYaw(wantedLocalYawDeg);

            //sync
            tile.setChanged();
            tile.updateClients();
        }
    }

    public static boolean hasValidTargetInCannonRange(Mob mob, int minCannonRange) {
        var target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        double distSq = mob.distanceToSqr(target);
        return distSq > minCannonRange * minCannonRange;
    }


}
