package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.SkullWithEyesModel;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {

    @WrapWithCondition(method = "renderSkull", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private static boolean supplementaries$renderSkullEyes(
            PoseStack poseStack, @Local(argsOnly = true) SkullModelBase model, @Local(argsOnly = true) MultiBufferSource bufferSource,
            @Local(argsOnly = true) int packedLight) {
        if (model instanceof SkullWithEyesModel sk) {
            sk.renderEyes(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        }
        return true;
    }

    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private static RenderType supplementaries$modifyDragonHeadRenderType(RenderType original,
                                                                         @Local ResourceLocation texture,
                                                                         @Local(argsOnly = true) SkullBlock.Type type) {
        if (type == ModRegistry.SPIDER_SKULL_TYPE || type == EndermanSkullBlock.TYPE) {
            return RenderType.entityCutoutNoCull(texture);
        }
        return original;
    }
}
