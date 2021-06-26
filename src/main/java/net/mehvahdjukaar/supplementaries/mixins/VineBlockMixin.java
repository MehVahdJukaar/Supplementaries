package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.minecraft.block.VineBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({VineBlock.class})
public abstract class VineBlockMixin {


    @Inject(method = "isAcceptableNeighbour", at = @At("HEAD"), cancellable = true)
    private static void isAcceptableNeighbour(IBlockReader world, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        if(direction==Direction.DOWN && world.getBlockState(pos).is(ModTags.VINE_SUPPORT))
            info.setReturnValue(true);

    }
}
