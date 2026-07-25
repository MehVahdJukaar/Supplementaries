package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.blocks.RelayerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DiodeBlock.class)
public abstract class DiodeBlockMixin extends HorizontalDirectionalBlock {

    protected DiodeBlockMixin(Properties properties) {
        super(properties);
    }

    // covers repeaters and comparators. Diodes only update the block they output into, so a relayer reading
    // them from any other side would keep a stale power level
    @Inject(method = "updateNeighborsInFront", at = @At("TAIL"))
    public void supp$updateSideRelayers(Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
        RelayerBlock.updateRelayersLookingAt(level, pos, this, state.getValue(DiodeBlock.FACING).getOpposite());
    }
}
