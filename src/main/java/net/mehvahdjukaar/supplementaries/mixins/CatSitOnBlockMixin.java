package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.blocks.PlanterBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CatSitOnBlockGoal.class)
public abstract class CatSitOnBlockMixin {

    @Inject(method = "shouldMoveTo", at = @At("HEAD"), cancellable = true)
    protected void shouldMoveTo(IWorldReader worldIn, BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        BlockState blockstate = worldIn.getBlockState(pos);
        if (blockstate.getBlock() instanceof PlanterBlock) {
            info.setReturnValue(true);
        }
    }


}
