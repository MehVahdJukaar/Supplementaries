package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.mehvahdjukaar.supplementaries.common.block.blocks.AshLayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    @Unique
    private BlockState bs;

    //TODO: add an event and move to lib

    @Inject(method = "checkBurnOut",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.removeBlock (Lnet/minecraft/core/BlockPos;Z)Z",
            shift = At.Shift.AFTER))
    private void afterRemoveBlock(Level level, BlockPos blockPos, int i, RandomSource randomSource, int j, CallbackInfo ci) {
        AshLayerBlock.tryConvertToAsh(level, blockPos, bs);
    }

    @Inject(method = "checkBurnOut",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.removeBlock (Lnet/minecraft/core/BlockPos;Z)Z"))
    private void beforeRemoveBlock(Level level, BlockPos blockPos, int i, RandomSource randomSource, int j, CallbackInfo ci) {
        bs = level.getBlockState(blockPos);
    }
}