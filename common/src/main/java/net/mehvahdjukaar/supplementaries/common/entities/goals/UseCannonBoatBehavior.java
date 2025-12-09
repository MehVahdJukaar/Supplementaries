package net.mehvahdjukaar.supplementaries.common.entities.goals;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
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
import net.minecraft.world.entity.raid.Raider;

import static net.mehvahdjukaar.supplementaries.common.entities.goals.PlundererAICommon.*;

public class UseCannonBoatBehavior extends Behavior<LivingEntity> {

    private int attackDelay;
    private CannonAccess access;

    public UseCannonBoatBehavior() {
        super(ImmutableMap.of(MemoryModuleType.HOME, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LAST_WOKEN, MemoryStatus.REGISTERED));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity owner) {
        Entity boat = owner.getControlledVehicle();
        if (boat instanceof CannonBoatEntity cb) {
            return cb.getInternalCannon().hasSomeFuelAndProjectiles();
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
            return cb.getInternalCannon().hasSomeFuelAndProjectiles();
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
        if (aimCannonAndShoot(access, (Raider) owner, livingentity, attackDelay <= 0)) {
            attackDelay = Mth.randomBetweenInclusive(level.random, SHOOTING_COOLDOWN_MIN, SHOOTING_COOLDOWN_MAX); //random delay between shots
        }
    }


    private void lookAtTarget(LivingEntity shooter, LivingEntity target) {
        shooter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
    }
}
