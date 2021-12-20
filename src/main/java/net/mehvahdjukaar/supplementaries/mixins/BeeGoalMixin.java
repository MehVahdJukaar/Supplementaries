package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//fixes, hopefully, double plant growth. should work with other mods too
@Mixin(targets = {"net.minecraft.world.entity.animal.Bee$BeeGrowCropGoal"})
public abstract class BeeGoalMixin {

    @Redirect(method = "tick",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.setBlockAndUpdate (Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
                    ordinal = 0))
    public boolean tick(Level level, BlockPos pos, BlockState state) {
        if (state.hasProperty(DoublePlantBlock.HALF)) {
            var p = state.getValue(DoublePlantBlock.HALF);
            BlockPos otherPos;
            if (p == DoubleBlockHalf.LOWER) {
                otherPos = pos.above();
            } else {
                otherPos = pos.below();
            }
            BlockState otherState = level.getBlockState(otherPos);

            Block b = otherState.getBlock();
            if (b == state.getBlock() && b instanceof CropBlock cropBlock) {
                int newAge = state.getValue(CropBlock.AGE);
                if (otherState.getValue(CropBlock.AGE) + 1 == newAge && !cropBlock.isMaxAge(otherState)) {
                    level.setBlock(otherPos, otherState.setValue(CropBlock.AGE, newAge), 2);
                }
            }
        }
        return level.setBlockAndUpdate(pos, state);
    }
}
