package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;

@Pseudo
@Mixin(targets = "vectorwing.farmersdelight.common.block.TomatoVineBlock")
public abstract class CompatFarmersDelightTomatoMixin extends Block {

    protected CompatFarmersDelightTomatoMixin(Properties arg) {
        super(arg);
    }

    @Inject(method = "attemptRopeClimb", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            shift = At.Shift.BEFORE), require = 1, remap = false, cancellable = true)
    public void supp$tomatoLoggingCompat(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {

        BlockState tomatoLogReplacement = FarmersDelightCompat.getTomatoLoggedReplacement(level, pos.above());

        if (tomatoLogReplacement != null) {
            int vineHeight;
            for (vineHeight = 1; level.getBlockState(pos.below(vineHeight)).getBlock() instanceof TomatoVineBlock; ++vineHeight) {
            }

            if (vineHeight < 3) {
                level.setBlockAndUpdate(pos.above(), tomatoLogReplacement);
            }
            ci.cancel();
        }
    }

    @ModifyReturnValue(method = "attemptRopeClimb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    public boolean supp$tomatoLoggingCompat2(boolean original, BlockState state) {
        return original || state.getBlock() instanceof TomatoVineBlock;
    }

}
