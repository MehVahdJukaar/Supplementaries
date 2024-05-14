package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.mehvahdjukaar.supplementaries.client.SelectableContainerItemHud;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHandler.class)
public abstract class MouseScrollMixin {

    @Inject(locals = LocalCapture.CAPTURE_FAILHARD,
            method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z",
    shift = At.Shift.BEFORE),   cancellable = true)
    public void onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci, double d0){
        if(SelectableContainerItemHud.isActive() && SelectableContainerItemHud.onMouseScrolled(d0)){
            ci.cancel();
        }
    }
}
