package net.mehvahdjukaar.supplementaries.common.entities.goals;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonTrajectory;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonUtils;
import net.mehvahdjukaar.supplementaries.common.block.cannon.ShootingMode;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class ManeuverAndShootCannonBehavior extends Behavior<LivingEntity> {

    private int attackDelay;
    private CannonAccess access;


    public ManeuverAndShootCannonBehavior() {
        super(ImmutableMap.of(MemoryModuleType.HOME, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LAST_WOKEN, MemoryStatus.REGISTERED));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity owner) {
        Entity boat = owner.getControlledVehicle();
        if (boat instanceof CannonBoatEntity cb) {
            return cb.getInternalCannon().hasFuelAndProjectiles();
        }
        LivingEntity livingentity = getAttackTarget(owner);
        return BehaviorUtils.canSee(owner, livingentity);
        //&& BehaviorUtils.isWithinAttackRange(owner, livingentity, 0);
    }

    private static LivingEntity getAttackTarget(LivingEntity shooter) {
        return shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity owner, long gameTime) {
        Entity boat = owner.getControlledVehicle();
        if (boat instanceof CannonBoatEntity cb) {
            return cb.getInternalCannon().hasFuelAndProjectiles();
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, LivingEntity owner, long gameTime) {
        Entity boat = owner.getControlledVehicle();
        if (boat instanceof CannonBoatEntity cb) {
            this.access = cb;
        }
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void stop(ServerLevel level, LivingEntity entity, long gameTime) {
        access = null;
    }


    @Override
    protected void tick(ServerLevel level, LivingEntity owner, long gameTime) {
        LivingEntity livingentity = getAttackTarget(owner);
        this.lookAtTarget(owner, livingentity);

        if (attackDelay > 0) {
            attackDelay--;
        }
        if (aimCannonAndShoot(access, owner, livingentity, attackDelay <= 0)) {
            attackDelay = Mth.randomBetweenInclusive(level.random, 20, 40); //random delay between shots
        }
    }

    public static boolean aimCannonAndShoot(CannonAccess access, LivingEntity shooter, LivingEntity target, boolean canShoot) {
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
                .add(0,0.6,0);

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

    private void lookAtTarget(LivingEntity shooter, LivingEntity target) {
        shooter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
    }
}
