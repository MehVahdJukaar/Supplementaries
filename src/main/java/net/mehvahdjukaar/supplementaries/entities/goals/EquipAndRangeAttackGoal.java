package net.mehvahdjukaar.supplementaries.entities.goals;

import net.mehvahdjukaar.supplementaries.entities.RedMerchantEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

public class EquipAndRangeAttackGoal extends Goal {

    private final ItemStack item;

    private final RedMerchantEntity mob;
    private LivingEntity target;
    private int attackTime = -1;
    private final double speedModifier;
    private int seeTime;
    private final int cooldown;
    private final int attackIntervalMin;
    private final int attackIntervalMax;
    private final float attackRadius;
    private final float attackRadiusSqr;



    public EquipAndRangeAttackGoal(RedMerchantEntity mob, double speed, int cooldown, int minInt, int maxInt, float range, ItemStack item) {
        this.mob = mob;
        this.cooldown = cooldown;
        this.speedModifier = speed;
        this.attackIntervalMin = minInt;
        this.attackIntervalMax = maxInt;
        this.attackRadius = range;
        this.attackRadiusSqr = range * range;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));

        this.item = item;
    }



    @Override
    public boolean canUse() {
        if(this.mob.attackCooldown > 0) return false;
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse(); // || !this.mob.getNavigation().isDone()
    }

    @Override
    public void stop() {
        this.mob.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
        this.target = null;
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public void start() {
        this.mob.setItemSlot(EquipmentSlotType.MAINHAND, this.item.copy());
        super.start();
    }

    @Override
    public void tick() {
        double d0 = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean flag = this.mob.getSensing().canSee(this.target);
        if (flag) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        if (!(d0 > (double)this.attackRadiusSqr) && this.seeTime >= 5) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().moveTo(this.target, this.speedModifier);
        }

        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        if (--this.attackTime == 0) {
            if (!flag) {
                return;
            }

            float f = MathHelper.sqrt(d0) / this.attackRadius;
            float lvt_5_1_ = MathHelper.clamp(f, 0.1F, 1.0F);
            this.mob.performRangedAttack(this.target, lvt_5_1_);
            this.attackTime = MathHelper.floor(f * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
            this.mob.attackCooldown = cooldown + mob.getRandom().nextInt(20);
        } else if (this.attackTime < 0) {
            float f2 = MathHelper.sqrt(d0) / this.attackRadius;
            this.attackTime = MathHelper.floor(f2 * (float) (this.attackIntervalMax - this.attackIntervalMin) + (float) this.attackIntervalMin);
        }


    }
}

