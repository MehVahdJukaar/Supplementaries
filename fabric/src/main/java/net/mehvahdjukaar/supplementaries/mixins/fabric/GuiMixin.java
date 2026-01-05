package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void supp$cannonCancelXPBar(GuiGraphics guiGraphics, int x, CallbackInfo ci) {
        if (CannonController.isActive()) ci.cancel();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    public void supp$cannonCancelHotbar(float partialTick, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (CannonController.isActive()) ci.cancel();
    }
}
