package net.mehvahdjukaar.supplementaries.mixins.compat;

import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.TomatoBlock;

// Hooks FD's extensible climbing API so the tomato crop climbs onto our rope/stick and turns into our
// compat blocks (which then keep climbing on their own, inheriting these methods). canClimbBlock keeps
// the bonemeal/validation paths aware of our blocks too.
@Pseudo
@Mixin(TomatoBlock.class)
public abstract class CompatFarmersDelightTomatoMixin extends Block {

    protected CompatFarmersDelightTomatoMixin(Properties arg) {
        super(arg);
    }

    @Inject(method = "getClimbingState", at = @At("HEAD"), cancellable = true, remap = false)
    public void supp$customClimbingState(BlockState stateAbove, CallbackInfoReturnable<BlockState> cir) {
        if (!CompatHandler.FARMERS_DELIGHT) return;
        BlockState replacement = FarmersDelightCompat.getClimbReplacement(stateAbove);
        if (replacement != null) {
            cir.setReturnValue(replacement);
        }
    }

    @Inject(method = "canClimbBlock", at = @At("HEAD"), cancellable = true, remap = false)
    public void supp$canClimbOurBlock(BlockState stateAbove, CallbackInfoReturnable<Boolean> cir) {
        if (!CompatHandler.FARMERS_DELIGHT) return;
        if (FarmersDelightCompat.canClimbOurBlock(stateAbove)) {
            cir.setReturnValue(true);
        }
    }
}
