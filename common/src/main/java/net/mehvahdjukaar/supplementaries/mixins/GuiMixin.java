package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.client.cannon.CannonChargeHud;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void suppl$cannonCrosshair(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CannonController.isActive()) {
            CannonChargeHud.renderCrossHair(graphics);
            ci.cancel();
        }
    }
}
