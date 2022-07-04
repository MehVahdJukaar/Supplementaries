package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.VineBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({VineBlock.class})
public abstract class VineMixin {


    //hurray trelices
    @Inject(method = "isAcceptableNeighbour", at = @At("HEAD"), cancellable = true)
    private static void isAcceptableNeighbour(BlockGetter world, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        if (direction == Direction.DOWN && world.getBlockState(pos).is(ModTags.VINE_SUPPORT))
            info.setReturnValue(true);
    }
}
