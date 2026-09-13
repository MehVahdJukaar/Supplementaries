package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.common.block.blocks.GrouperBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.RelayerBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RedStoneWireBlock.class)
public abstract class RedstoneWireBlockMixin {

    @ModifyReturnValue(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
            at = @At(value = "RETURN"))
    private static boolean supp$connectToRelayer(boolean original, BlockState state, Direction direction) {
        Block block = state.getBlock();
        if (block instanceof RelayerBlock || block instanceof GrouperBlock) {
            return state.getValue(RelayerBlock.FACING) == direction;
        }
        return original;
    }
}
