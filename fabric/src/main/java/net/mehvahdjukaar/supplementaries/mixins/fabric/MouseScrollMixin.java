package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.client.hud.SelectableContainerItemHud;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseScrollMixin {

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z",
            shift = At.Shift.BEFORE), cancellable = true)
    public void supp$onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci, @Local(ordinal = 4) double dy) {
        if (SelectableContainerItemHud.getInstance().isActive() && SelectableContainerItemHud.getInstance().onMouseScrolled(dy)) {
            ci.cancel();
        }
    }
}
