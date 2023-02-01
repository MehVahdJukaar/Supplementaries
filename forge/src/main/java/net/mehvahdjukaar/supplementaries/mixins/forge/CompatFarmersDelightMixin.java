package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "vectorwing.farmersdelight.common.block.TomatoVineBlock")
public abstract class CompatFarmersDelightMixin extends Block {

    protected CompatFarmersDelightMixin(Properties arg) {
        super(arg);
    }

    //break protection
    @Inject(method = "attemptRopeClimb", at = @At(value = "INVOKE",
            ordinal = 14,
            shift = At.Shift.BEFORE), cancellable = true, require = 0, remap = false)
    public void suppRopeCompat(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (FarmersDelightCompat.tryTomatoLogging(level, pos.above())) ci.cancel();
    }
}
