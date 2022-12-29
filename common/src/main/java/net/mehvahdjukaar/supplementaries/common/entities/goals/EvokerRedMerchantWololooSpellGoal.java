package net.mehvahdjukaar.supplementaries.common.entities.goals;


import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.npc.WanderingTrader;

import java.util.List;

public class EvokerRedMerchantWololooSpellGoal extends Goal {

    private final TargetingConditions selector = TargetingConditions.forNonCombat().range(16.0);

    private final Evoker evoker;
    private final ISuppEvoker suppEvoker;

    public EvokerRedMerchantWololooSpellGoal(Evoker evoker) {
        super();
        this.evoker = evoker;
        this.suppEvoker = (ISuppEvoker) evoker;
    }

    @Override
    public boolean canUse() {
        if (evoker.getTarget() != null) {
            return false;
        } else if (evoker.isCastingSpell()) {
            return false;
        } else if (evoker.tickCount < this.nextAttackTickCount) {
            return false;
        } else if (!PlatformHelper.isMobGriefingOn(evoker.level, evoker)) {
            return false;
        } else {
            List<WanderingTrader> list = evoker.level.getNearbyEntities(WanderingTrader.class, this.selector, evoker, evoker.getBoundingBox().inflate(16.0, 4.0, 16.0));
            if (list.isEmpty()) {
                return false;
            } else {
                ((ISuppEvoker) evoker).setCustomWololoo(list.get(evoker.getRandom().nextInt(list.size())));
                return true;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return suppEvoker.getCustomWololoo() != null && this.attackWarmupDelay > 0;
    }

    @Override
    public void stop() {
        super.stop();
        suppEvoker.setCustomWololoo(null);
    }

    protected void performSpellCasting() {
        LivingEntity entity = suppEvoker.getCustomWololoo();
        if (entity != null && entity.isAlive() && ForgeHelper.canLivingConvert(entity, ModEntities.RED_MERCHANT.get(),
                (timer) -> {
                })) {
            if (!entity.isRemoved()) {
                var mob = ModEntities.RED_MERCHANT.get().create(entity.level);
                if (mob != null) {
                    var tag = new CompoundTag();
                    entity.saveWithoutId(tag);
                    tag.remove("Offers");
                    mob.load(tag);

                    entity.discard();
                    mob.removeEffect(MobEffects.INVISIBILITY);
                    entity.level.addFreshEntity(mob);
                }
            }
        }
    }

    protected int getCastWarmupTime() {
        return 40;
    }

    protected int getCastingTime() {
        return 60;
    }

    protected int getCastingInterval() {
        return 140;
    }

    protected SoundEvent getSpellPrepareSound() {
        return SoundEvents.EVOKER_PREPARE_WOLOLO;
    }

    protected SpellcasterIllager.IllagerSpell getSpell() {
        return SpellcasterIllager.IllagerSpell.WOLOLO;
    }


    protected int attackWarmupDelay;
    protected int nextAttackTickCount;

    @Override
    public void start() {
        this.attackWarmupDelay = this.adjustedTickDelay(this.getCastWarmupTime());
        suppEvoker.setSpellCastingTime(this.getCastingTime());
        this.nextAttackTickCount = evoker.tickCount + this.getCastingInterval();
        SoundEvent soundEvent = this.getSpellPrepareSound();
        if (soundEvent != null) {
            evoker.playSound(soundEvent, 1.0F, 1.0F);
        }

        evoker.setIsCastingSpell(this.getSpell());
    }

    @Override
    public void tick() {
        --this.attackWarmupDelay;
        if (this.attackWarmupDelay == 0) {
            this.performSpellCasting();
            evoker.playSound(evoker.getCastingSoundEvent(), 1.0F, 1.0F);
        }

    }
}
