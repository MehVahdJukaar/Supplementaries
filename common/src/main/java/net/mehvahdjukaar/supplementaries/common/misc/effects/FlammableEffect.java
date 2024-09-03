package net.mehvahdjukaar.supplementaries.common.misc.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class FlammableEffect extends MobEffect {

    public FlammableEffect() {
        super(MobEffectCategory.HARMFUL, 0xdd4400);
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {

        int ticks = pLivingEntity.getRemainingFireTicks();
        if (ticks <= 0) return;
        if (ticks <= 8 * 20) {
            pLivingEntity.setSecondsOnFire(8);
        }

        Level level = pLivingEntity.level();

        if (pLivingEntity.getRandom().nextFloat() > 0.5f + (pAmplifier * 0.2)) {
            return;
        }
        FireBlock delegate = (FireBlock) Blocks.FIRE;

        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            var positions = BlockPos.betweenClosedStream(pLivingEntity.getBoundingBox())
                    .map(BlockPos::immutable).distinct().collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(positions);
            for (BlockPos pos : positions) {
                if (level.isRaining() && delegate.isNearRain(level, pos)) {
                    continue;
                }
                for (Direction d : Direction.values()) {
                    if (BaseFireBlock.canBePlacedAt(level, pos, d)) {
                        //int j2 = Math.min(15, i + random.nextInt(5) / 4);
                        BlockState state = BaseFireBlock.getState(level, pos);
                        level.setBlock(pos, state, 3);
                    }
                }
                break;
            }

        }

            /*
            FireBlock fireblock = (FireBlock) BaseFireBlock.FIRE.get();
            if (BaseFireBlock.canBePlacedAt(level, blockpos, direction)) {
                //TODO: soulfire mod compat
                level.setBlockAndUpdate(blockpos, BaseFireBlock.getState(level, blockpos));
            }*/
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
