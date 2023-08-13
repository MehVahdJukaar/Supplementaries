package net.mehvahdjukaar.supplementaries.mixins.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.ColorUtil;
import net.mehvahdjukaar.moonlight.api.misc.OptionalMixin;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//fixes plain text shade
@OptionalMixin("vectorwing.farmersdelight.client.renderer.CanvasSignRenderer")
@Mixin(targets = "vectorwing.farmersdelight.client.renderer.CanvasSignRenderer")
public abstract class CompatFarmersDelightSignMixin {

    @Unique
    private static Float supplementaries$canvasSignYaw;
    @Unique
    private static Boolean supplementaries$canvasFront;

    /**
     * @author MehVahDjukaar
     * @reason adding color normal shading and color modifier. Need override because we could loose precision due to int conversion if done on return
     */
    @Overwrite(remap = false)
    public static int getDarkColor(SignText signText, boolean isOutlineVisible) {
        int color = signText.getColor().getTextColor();
        if (color == DyeColor.BLACK.getTextColor() && signText.hasGlowingText()) {
            return -988212;
        } else {
            float brightness = isOutlineVisible ? 0.4f : 0.6f;
            float scale = (brightness * ClientConfigs.getSignColorMult());
            if (supplementaries$canvasFront != null && supplementaries$canvasSignYaw != null) {
                Vector3f normal = new Vector3f(0, 0, 1);
                normal.rotateY(supplementaries$canvasSignYaw * Mth.DEG_TO_RAD * (supplementaries$canvasFront ? 1 : -1));
                supplementaries$canvasFront = null;
                scale *= ColorUtil.getShading(normal);
            }
            return ColorUtil.multiply(color, scale);
        }
    }

    @Inject(method = "translateSign", at = @At("HEAD"))
    private void captureYaw(PoseStack poseStack, float yaw, BlockState blockState, CallbackInfo ci) {
        supplementaries$canvasSignYaw = yaw;
    }

    @Inject(method = "renderSignText", at = @At("HEAD"))
    private void captureFace(BlockPos blockPos, SignText signText, PoseStack poseStack, MultiBufferSource multiBufferSource,
                             int i, int j, int k, boolean face, CallbackInfo ci) {
        supplementaries$canvasFront = face;
    }

    @Inject(method = "renderSignWithText", at = @At("TAIL"), remap = false)
    private void resetYaw(SignBlockEntity signBlockEntity, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, BlockState state, SignBlock block, DyeColor dye, Model model, CallbackInfo ci) {
        supplementaries$canvasSignYaw = null;
    }
}
