package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockMixin {

    @Inject(method = "getOffset", at = @At(
            value = "RETURN",
            ordinal = 1),
            cancellable = true)
    public void getOffset(IBlockReader world, BlockPos pos, CallbackInfoReturnable<Vector3d> cir) {
        if(cir.getReturnValue() != Vector3d.ZERO && world.getBlockState(pos.below()).is(ModRegistry.PLANTER.get())){
            cir.setReturnValue(Vector3d.ZERO);
        }
    }
}
