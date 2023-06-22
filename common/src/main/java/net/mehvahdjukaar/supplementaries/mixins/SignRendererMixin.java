package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.ColorUtil;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.supplementaries.client.TextUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//fixes plain text shade
@Mixin(SignRenderer.class)
public abstract class SignRendererMixin {

    @Unique
    private static Float signYaw;
    @Unique
    private static Boolean front;

    @Inject(method = "getDarkColor", at = @At("RETURN"), cancellable = true)
    private static void getShadedColor(SignText signText, CallbackInfoReturnable<Integer> cir) {
        int ret = cir.getReturnValue();
        if (ret != -988212 && front != null && signYaw != null) {
            //doesnt account for glowing. too bad
            Vector3f normal = new Vector3f(0, 0, 1);
            normal.rotateY(signYaw * Mth.DEG_TO_RAD * (front ? 1 : -1));
            cir.setReturnValue(TextUtil.adjustTextColor(ret, normal));
            front = null;
            signYaw = null;
        }
    }

    @Inject(method = "translateSign", at = @At("HEAD"))
    private void captureYaw(PoseStack poseStack, float yaw, BlockState blockState, CallbackInfo ci) {
        signYaw = yaw;
    }

    @Inject(method = "renderSignText", at = @At("HEAD"))
    private void captureFace(BlockPos blockPos, SignText signText, PoseStack poseStack, MultiBufferSource multiBufferSource,
                             int i, int j, int k, boolean face, CallbackInfo ci) {
        front = face;
    }
}
