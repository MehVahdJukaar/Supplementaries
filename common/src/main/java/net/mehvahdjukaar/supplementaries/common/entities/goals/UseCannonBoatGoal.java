package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

import static net.mehvahdjukaar.supplementaries.common.entities.goals.UseCannonAICommon.*;

//TOOD: uniform with cannon ai common
//copied from skeleton soot goal
public class UseCannonBoatGoal extends Goal {

    private final int maxTimeWithoutShooting;
    private final int minCannonRange;
    private final int shootingCooldownMin;
    private final int shootingCooldownMax;

    private final Mob mob;

    private final int maxUseTime;

    private int time;
    @Nullable
    private LivingEntity target;
    private int igniteCannonCooldown;
    private int ticksSinceShot;
    private CannonAccess access;

    @Deprecated(forRemoval = true)
    public UseCannonBoatGoal(Mob mob, int a, int b, int minRange, int maxDuration) {
        this(mob, maxDuration, minRange, a, b, MAX_TIME_WITHOUT_SHOOTING);
    }

    public UseCannonBoatGoal(Mob mob,int maxDuration) {
        this(mob, maxDuration, MIN_CANNON_RANGE, SHOOTING_COOLDOWN_MIN, SHOOTING_COOLDOWN_MAX, MAX_TIME_WITHOUT_SHOOTING);
    }

    public UseCannonBoatGoal(Mob mob,int maxDuration, int minRange, int minShootingCooldown, int maxShootingCooldown, int maxTimeWithoutShooting) {
        this.mob = mob;
        this.maxUseTime = maxDuration;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));

        this.minCannonRange = minRange;
        this.shootingCooldownMin = minShootingCooldown;
        this.shootingCooldownMax = maxShootingCooldown;
        this.maxTimeWithoutShooting = maxTimeWithoutShooting;

    }

    @Override
    public boolean canUse() {
        if (this.mob.getNavigation().isStuck()) return false;
        if (mob.getControlledVehicle() instanceof CannonAccess ac &&
                ac.getInternalCannon().hasSomeFuelAndProjectiles() &&
                UseCannonAICommon.hasValidTargetInCannonRange(this.mob, minCannonRange)) {
            this.access = ac;
            this.target = this.mob.getTarget();
            return true;
        }
        return false;
    }

    //also calls can use
    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.time < this.maxUseTime && ticksSinceShot < maxTimeWithoutShooting;
    }

    @Override
    public void stop() {
        this.target = null;
        this.access = null;
        this.ticksSinceShot = 0;
        this.igniteCannonCooldown = 0;
        this.time = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.time++;

        this.mob.getLookControl().setLookAt(this.target, 90, 90);
        this.ticksSinceShot++;

        if (igniteCannonCooldown > 0) {
            igniteCannonCooldown--;
        }
        if (aimCannonAndShoot(access, this.mob, this.target, igniteCannonCooldown <= 0)) {
            igniteCannonCooldown = shootCooldown();
            ticksSinceShot = 0;
        }
    }

    private int shootCooldown(){
        return Mth.randomBetweenInclusive(mob.getRandom(), shootingCooldownMin,shootingCooldownMax);
    }

}