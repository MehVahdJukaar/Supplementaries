package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.blocks.AshLayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    @Unique
    private BlockState bs;

    @Inject(method = "tryCatchFire",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.removeBlock (Lnet/minecraft/core/BlockPos;Z)Z",
            shift = At.Shift.AFTER))
    private void afterRemoveBlock(Level pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face, CallbackInfo ci) {
        AshLayerBlock.tryConvertToAsh(pLevel, pPos, bs);
    }

    @Inject(method = "tryCatchFire",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/level/Level.removeBlock (Lnet/minecraft/core/BlockPos;Z)Z"))
    private void beforeRemoveBlock(Level pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face, CallbackInfo ci) {
        bs = pLevel.getBlockState(pPos);
    }

    


}