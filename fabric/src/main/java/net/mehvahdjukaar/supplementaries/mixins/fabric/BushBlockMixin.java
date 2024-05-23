package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PlanterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BushBlock.class)
public abstract class BushBlockMixin {

    @ModifyReturnValue(method = "mayPlaceOn", at = @At("RETURN"))
    public boolean mayPlaceOn(boolean original, BlockState state, BlockGetter level, BlockPos pos) {
        return !original && state.getBlock() instanceof PlanterBlock;
    }
}
