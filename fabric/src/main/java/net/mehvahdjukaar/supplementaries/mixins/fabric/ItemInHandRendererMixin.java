package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.mixin.client.rendering.InGameHudMixin;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    public void supp$cancelHandRendererCannon(float partialTicks, PoseStack poseStack, MultiBufferSource.BufferSource buffer, LocalPlayer playerEntity, int combinedLight, CallbackInfo ci) {
        if (CannonController.isActive()) ci.cancel();
    }
}
