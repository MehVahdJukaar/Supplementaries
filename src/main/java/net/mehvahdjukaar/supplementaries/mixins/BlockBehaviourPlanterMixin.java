package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehaviourPlanterMixin {

    @Inject(method = "getOffset", at = @At(
            value = "RETURN",
            ordinal = 1),
            cancellable = true)
    public void getOffset(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Vec3> cir) {
        //null check for world since some mods like to throw a null world here...
        if (world != null && cir.getReturnValue() != Vec3.ZERO && world.getBlockState(pos.below()).is(ModRegistry.PLANTER.get())) {
            cir.setReturnValue(Vec3.ZERO);
        }
    }
}
