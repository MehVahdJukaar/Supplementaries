package net.mehvahdjukaar.supplementaries.mixins.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
@Mixin(TomatoVineBlock.class)
public abstract class CompatFarmersDelightTomatoMixin extends Block {

    protected CompatFarmersDelightTomatoMixin(Properties arg) {
        super(arg);
    }

    @Inject(method = "attemptRopeClimb",
            remap = false,
            at = @At(value = "INVOKE",
                    remap = true,
                    target = "Lnet/minecraft/core/BlockPos;above()Lnet/minecraft/core/BlockPos;"), require = 1,
            cancellable = true)
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

    @WrapOperation(method = "attemptRopeClimb",
            remap = false,
            at = @At(value = "INVOKE",
                    remap = true,
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    public boolean supp$tomatoLoggingCompat2(BlockState state, Block block, Operation<Boolean> operation) {
        return state.getBlock() instanceof TomatoVineBlock || operation.call(state, block);
    }

}
