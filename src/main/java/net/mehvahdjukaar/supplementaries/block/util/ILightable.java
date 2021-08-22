package net.mehvahdjukaar.supplementaries.block.util;

import net.minecraft.block.BlockState;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface ILightable {

    boolean lightUp(BlockState state, BlockPos pos, IWorld world, FireSound sound);

    boolean extinguish(BlockState state, BlockPos pos, IWorld world);

    enum FireSound{
        FLINT_AND_STEEL,
        FIRE_CHANGE,
        FLAMING_ARROW;
        public void play(IWorld world, BlockPos pos){
            switch(this){
                case FIRE_CHANGE:
                    world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.2F + 1.0F);
                    break;
                case FLAMING_ARROW:
                    world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundCategory.BLOCKS, 0.5F, 1.4F);
                    break;
                case FLINT_AND_STEEL:
                    world.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                    break;
            }
        }
    }
}
