package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.block.blocks.IronGateBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.WickerFenceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({IronBarsBlock.class})
public abstract class IronBarsBlockMixin {


    @ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
    private BlockState supp$alterPlacementState(BlockState original, @Local(argsOnly = true) BlockPlaceContext context) {
        return IronGateBlock.messWithIronBarsState(context.getLevel(), context.getClickedPos(), original);
    }

    @ModifyReturnValue(method = "updateShape", at = @At("RETURN"))
    private BlockState supp$alterUpdateShape(BlockState original, @Local(argsOnly = true) LevelAccessor level, @Local(ordinal = 0, argsOnly = true) BlockPos pos) {
        return IronGateBlock.messWithIronBarsState(level, pos, original);
    }

    @ModifyReturnValue(method = "attachsTo", at = @At("RETURN"))
    private boolean supp$alterAttachsTo(boolean original, BlockState state, boolean solidSide) {
        return original || state.getBlock() instanceof WickerFenceBlock;
    }

}
