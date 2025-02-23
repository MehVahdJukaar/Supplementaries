package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.client.hud.SelectableContainerItemHud;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow private double accumulatedDX;

    @WrapWithCondition(method = "turnPlayer",
            at = @At(target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V", value = "INVOKE")
    )
    protected boolean supp$onMoveMouse(LocalPlayer instance, double yRot, double xRot) {
        return !CannonController.onPlayerRotated(yRot, xRot);
    }

    // doesnt care about smooth panning in camera. that's why its here
    @Inject(method = "handleAccumulatedMovement", at = @At("HEAD")
    )
    protected void supp$onMoveMouse(CallbackInfo ci) {
        SelectableContainerItemHud.getInstance().ohMouseMoved(this.accumulatedDX);
    }
}
