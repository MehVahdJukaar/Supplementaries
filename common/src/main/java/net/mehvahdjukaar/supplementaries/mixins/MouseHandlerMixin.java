package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.mehvahdjukaar.supplementaries.client.QuiverArrowSelectGui;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    private double xpos;

    @WrapWithCondition(method = "turnPlayer",
            at = @At(target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V", value = "INVOKE")
    )
    protected boolean onMoveMouse(LocalPlayer instance, double yRot, double xRot) {
        if(CannonController.isActive()){
            CannonController.onPlayerRotated(yRot, xRot);
            return false;
        }
        return true;
    }

    // doesnt care about smooth panning in camera. that's why its here
    @Inject(method = "onMove",
            at = @At(target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V", value = "INVOKE",
                    shift = At.Shift.BEFORE)
    )
    protected void onMoveMouse(long windowPointer, double xpos, double ypos, CallbackInfo ci) {
        if (QuiverArrowSelectGui.isActive() && ClientConfigs.Items.QUIVER_MOUSE_MOVEMENT.get()) {
            double deltaX = xpos - this.xpos;
            QuiverArrowSelectGui.ohMouseMoved(deltaX);
        }
    }
}
