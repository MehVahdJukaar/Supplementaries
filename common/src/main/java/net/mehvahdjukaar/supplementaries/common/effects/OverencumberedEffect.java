package net.mehvahdjukaar.supplementaries.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;

public class OverencumberedEffect extends MobEffect {

    public OverencumberedEffect() {
        super(MobEffectCategory.HARMFUL, 0x6C451F);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "2303DE5E-8CE8-4030-940E-614C1F160830",
                -0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {

    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity pSource, @Nullable Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth) {
    }

    @Override
    public void addAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        if (pAmplifier > 1) {
            super.addAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier - 2);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity pLivingEntity, AttributeMap pAttributeMap, int pAmplifier) {
        super.removeAttributeModifiers(pLivingEntity, pAttributeMap, pAmplifier - 2);
    }
}
