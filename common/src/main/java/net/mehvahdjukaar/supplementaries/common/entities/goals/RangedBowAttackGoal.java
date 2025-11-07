package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

public class RangedBowAttackGoal<T extends Monster & RangedAttackMob> extends Goal {
    private final T mob;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public RangedBowAttackGoal(T mob, double speedModifier, int attackIntervalMin, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public void setMinAttackInterval(int attackCooldown) {
        this.attackIntervalMin = attackCooldown;
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() == null ? false : this.isHoldingBow();
    }

    protected boolean isHoldingBow() {
        return this.mob.isHolding(Items.BOW);
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingBow();
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity != null) {
            double distanceSqrt = this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(livingEntity);
            boolean sawIt = this.seeTime > 0;
            if (hasLineOfSight != sawIt) {
                this.seeTime = 0;
            }

            if (hasLineOfSight) {
                this.seeTime++;
            } else {
                this.seeTime--;
            }

            if (!(distanceSqrt > this.attackRadiusSqr) && this.seeTime >= 20) {
                this.mob.getNavigation().stop();
                this.strafingTime++;
            } else {
                this.mob.getNavigation().moveTo(livingEntity, this.speedModifier);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20) {
                if (this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if (this.mob.getRandom().nextFloat() < 0.3) {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1) {
                if (distanceSqrt > this.attackRadiusSqr * 0.75F) {
                    this.strafingBackwards = false;
                } else if (distanceSqrt < this.attackRadiusSqr * 0.25F) {
                    this.strafingBackwards = true;
                }

                this.mob.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                if (this.mob.getControlledVehicle() instanceof Mob mob) {
                    mob.lookAt(livingEntity, 30.0F, 30.0F);
                }

                this.mob.lookAt(livingEntity, 30.0F, 30.0F);
            } else {
                this.mob.getLookControl().setLookAt(livingEntity, 30.0F, 30.0F);
            }

            if (this.mob.isUsingItem()) {
                if (!hasLineOfSight && this.seeTime < -60) {
                    this.mob.stopUsingItem();
                } else if (hasLineOfSight) {
                    int i = this.mob.getTicksUsingItem();
                    if (i >= 20) {
                        this.mob.stopUsingItem();
                        this.mob.performRangedAttack(livingEntity, BowItem.getPowerForTime(i));
                        this.attackTime = this.attackIntervalMin;
                    }
                }
            } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
                this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.BOW));
            }
        }
    }
}

