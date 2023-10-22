package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.blocks.IronGateBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({IronBarsBlock.class})
public abstract class IronBarsBlockMixin {


    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState altered = IronGateBlock.messWithIronBarsState(context.getLevel(), context.getClickedPos(), cir.getReturnValue());
        if(altered != null) cir.setReturnValue(altered);
    }


    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    private void updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        BlockState altered = IronGateBlock.messWithIronBarsState(level, pos, cir.getReturnValue());
        if(altered != null) cir.setReturnValue(altered);
    }
}
