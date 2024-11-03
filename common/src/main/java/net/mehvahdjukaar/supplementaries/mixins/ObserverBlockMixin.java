package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.block.blocks.MovingSlidyBlockSource;
import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingSlidyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ObserverBlock.class)
public class ObserverBlockMixin {


    @WrapWithCondition(method = "updateShape",
            at = @At(value =
                    "INVOKE", target = "Lnet/minecraft/world/level/block/ObserverBlock;startSignal(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V"))
    public boolean supp$preventSlidyBlockUpdates(ObserverBlock instance, LevelAccessor level, BlockPos pos,
                                                 @Local(ordinal = 1, argsOnly = true) BlockState neighbor) {
        return !MovingSlidyBlockEntity.shouldCancelObserverUpdateHack(neighbor);
    }
}
