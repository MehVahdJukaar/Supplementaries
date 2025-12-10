package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.frozenblock.wilderwild.mixin.projectile.ThrownEnderpearlMixin;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void supp$cannonCancelXPBar(GuiGraphics guiGraphics, int x, CallbackInfo ci) {
        if (CannonController.cancelsXPBar()) ci.cancel();
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    public void vista$cancelXPLevel(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CannonController.cancelsXPBar()) ci.cancel();
    }

    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    public void supp$cannonCancelHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (CannonController.cancelsHotBar()) ci.cancel();
    }
}
