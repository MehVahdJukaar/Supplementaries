package net.mehvahdjukaar.supplementaries.common.misc.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.FireBlock;
import org.jetbrains.annotations.Nullable;

public class FlammableEffect extends MobEffect {

    public FlammableEffect() {
        super(MobEffectCategory.HARMFUL, 0xdd4400);
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {

        int ticks = pLivingEntity.getRemainingFireTicks();
        if (ticks > 0 && pLivingEntity.tickCount % 3 == 0) {
            pLivingEntity.setRemainingFireTicks(ticks + 1);
        }

        if(ticks> 0 && pLivingEntity.tickCount+4 % 20 == 0){
            Level level = pLivingEntity.level();
            /*
            FireBlock fireblock = (FireBlock) BaseFireBlock.FIRE.get();
            if (BaseFireBlock.canBePlacedAt(level, blockpos, direction)) {
                //TODO: soulfire mod compat
                level.setBlockAndUpdate(blockpos, BaseFireBlock.getState(level, blockpos));
            }*/
        }
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity pSource, @Nullable Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth) {

    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
    }
}
