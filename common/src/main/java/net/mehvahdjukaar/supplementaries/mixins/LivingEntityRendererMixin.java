package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonRiderRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @WrapWithCondition(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    private boolean suppl$skipRiderBodyLayers(RenderLayer<?, ?> layer, PoseStack poseStack, MultiBufferSource buffer,
                                              int packedLight, Entity entity, float limbSwing,
                                              float limbSwingAmount, float partialTick, float ageInTicks,
                                              float netHeadYaw, float headPitch) {
        if (!CannonRiderRenderer.isRenderingFromCannon()) return true;
        return layer instanceof HumanoidArmorLayer<?, ?, ?> || layer instanceof CustomHeadLayer<?, ?>
                || layer instanceof Deadmau5EarsLayer;
    }
}
