package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.ColorUtil;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//fixes plain text shade
@Mixin(SignRenderer.class)
public abstract class SignRendererMixin {

    @Unique
    private static Float supplementaries$signYaw;
    @Unique
    private static Boolean supplementaries$front;

    //screw this

    /**
     * @author MehVahDjukaar
     * @reason adding color normal shading and color modifier
     */
    @Overwrite
    public static int getDarkColor(SignText signText) {
        int color = signText.getColor().getTextColor();
        if (color == DyeColor.BLACK.getTextColor() && signText.hasGlowingText()) {
            return -988212;
        } else {
            float scale = (0.4f * ClientConfigs.getSignColorMult());
            if (supplementaries$front != null && supplementaries$signYaw != null) {
                Vector3f normal = new Vector3f(0, 0, 1);
                normal.rotateY(supplementaries$signYaw * Mth.DEG_TO_RAD * (supplementaries$front ? 1 : -1));
                supplementaries$front = null;
                scale *= ColorUtil.getShading(normal);
            }
            return ColorUtil.multiply(color, scale);
        }
    }


    @Inject(method = "translateSign", at = @At("HEAD"))
    private void captureYaw(PoseStack poseStack, float yaw, BlockState blockState, CallbackInfo ci) {
        supplementaries$signYaw = yaw;
    }

    @Inject(method = "renderSignText", at = @At("HEAD"))
    private void captureFace(BlockPos blockPos, SignText signText, PoseStack poseStack, MultiBufferSource multiBufferSource,
                             int i, int j, int k, boolean face, CallbackInfo ci) {
        supplementaries$front = face;
    }

    @Inject(method = "renderSignWithText", at = @At("TAIL"))
    private void resetYaw(SignBlockEntity signBlockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BlockState blockState, SignBlock signBlock, WoodType woodType, Model model, CallbackInfo ci) {
        supplementaries$signYaw = null;
    }
}
