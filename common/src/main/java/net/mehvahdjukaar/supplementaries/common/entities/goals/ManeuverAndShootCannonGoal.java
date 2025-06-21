package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.vehicle.Boat;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

import static net.mehvahdjukaar.supplementaries.common.entities.goals.ManeuverAndShootCannonBehavior.aimCannonAndShoot;

public class ManeuverAndShootCannonGoal extends Goal {
    private final Mob mob;
    private final int attackIntervalMin;
    private final int attackIntervalMax;

    @Nullable
    private LivingEntity target;
    private int attackDelay;
    private int seeTime;
    private CannonAccess access;

    public ManeuverAndShootCannonGoal(Mob rangedAttackMob, int attackIntervalMin, int attackIntervalMax) {
        this.mob = rangedAttackMob;
        this.attackIntervalMin = attackIntervalMin;
        this.attackIntervalMax = attackIntervalMax;
        this.setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity != null && livingEntity.isAlive() &&
                mob.getControlledVehicle() instanceof CannonAccess ac &&
                ac.getInternalCannon().hasFuelAndProjectiles()) {
            this.access = ac;
            this.target = livingEntity;
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || this.target.isAlive() && !this.mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        this.target = null;
        this.access = null;
        this.seeTime = 0;
        this.attackDelay = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(this.target);
        if (hasLineOfSight) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        this.mob.getLookControl().setLookAt(this.target, 90, 90);

        if (attackDelay > 0) {
            attackDelay--;
        }
        if (!hasLineOfSight) {
            return;
        }
        if (aimCannonAndShoot(access, this.mob, this.target, attackDelay <= 0)) {
            attackDelay = Mth.randomBetweenInclusive(this.mob.getRandom(), attackIntervalMin, attackIntervalMax); //random delay between shots
        }
    }
}