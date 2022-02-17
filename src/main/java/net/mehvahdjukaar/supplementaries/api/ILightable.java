package net.mehvahdjukaar.supplementaries.api;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

//implement to have your block be lightable by gunpowder
public interface ILightable {

    boolean lightUp(@Nullable Entity entity, BlockState state, BlockPos pos, LevelAccessor world, FireSound sound);

    boolean extinguish(@Nullable Entity entity, BlockState state, BlockPos pos, LevelAccessor world);

    enum FireSound{
        FLINT_AND_STEEL,
        FIRE_CHANGE,
        FLAMING_ARROW;
        public void play(LevelAccessor world, BlockPos pos){
            switch (this) {
                case FIRE_CHANGE -> world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.2F + 1.0F);
                case FLAMING_ARROW -> world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5F, 1.4F);
                case FLINT_AND_STEEL -> world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            }
        }
    }
}
