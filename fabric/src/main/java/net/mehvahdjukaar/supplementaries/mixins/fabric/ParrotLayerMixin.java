package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.PlundererParrotOnShoulderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ParrotOnShoulderLayer.class)
public abstract class ParrotLayerMixin<T extends Player> {

    @Shadow
    @Final
    private ParrotModel model;

    @WrapOperation(method = "method_17958",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ParrotModel;renderOnShoulder(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFFI)V"))
    private void supp$renderParty(ParrotModel instance, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch, int tickCount, Operation<Void> original,
                                  @Local(argsOnly = true) CompoundTag compoundTag) {
        if (compoundTag.getBoolean("record_playing")) {
            PlundererParrotOnShoulderLayer.renderDancing(model, poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY, limbSwing, limbSwingAmount, netHeadYaw, headPitch,
                    (int) Minecraft.getInstance().level.getGameTime(), 0);
        } else {
            original.call(instance, poseStack, buffer, packedLight, packedOverlay, limbSwing, limbSwingAmount, netHeadYaw, headPitch, tickCount);
        }
    }

}
