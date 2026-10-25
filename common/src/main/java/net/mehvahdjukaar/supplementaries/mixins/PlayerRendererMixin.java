package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonRiderRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.PicklePlayer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.CannonFlight;
import net.mehvahdjukaar.supplementaries.common.entities.ICannonRider;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void suppl$letCannonDrawRider(AbstractClientPlayer entity, float entityYaw, float partialTick,
                                          PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                          CallbackInfo ci) {
        if (!CannonRiderRenderer.isRenderingFromCannon() && CannonBlockTile.riddenBy(entity) != null) {
            ci.cancel();
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;setModelProperties(Lnet/minecraft/client/player/AbstractClientPlayer;)V"),
            cancellable = true)
    private void suppl$pickleRick(AbstractClientPlayer entity, float entityYaw, float partialTick,
                                  PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                  CallbackInfo ci) {
        if (PicklePlayer.renderInstead((PlayerRenderer) (Object) this, entity, partialTick,
                poseStack, buffer, packedLight)) {
            ci.cancel();
        }
    }

    @Inject(method = "setModelProperties", at = @At("TAIL"))
    private void suppl$onlyShowRiderHead(AbstractClientPlayer player, CallbackInfo ci) {
        if (!CannonRiderRenderer.isRenderingFromCannon()) return;
        PlayerModel<AbstractClientPlayer> model = ((PlayerRenderer) (Object) this).getModel();
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
    }

    @Inject(method = "setupRotations", at = @At("HEAD"), cancellable = true)
    private void suppl$keepRiderAlignedWithBarrel(AbstractClientPlayer entity, PoseStack poseStack, float bob,
                                                  float yBodyRot, float partialTick, float scale, CallbackInfo ci) {
        if (CannonRiderRenderer.isRenderingFromCannon()) ci.cancel();
    }

    @Inject(method = "setupRotations", at = @At("HEAD"), cancellable = true)
    private void suppl$faceAlongCannonFlight(AbstractClientPlayer entity, PoseStack poseStack, float bob,
                                             float yBodyRot, float partialTick, float scale, CallbackInfo ci) {
        CannonFlight flight = ((ICannonRider) entity).supplementaries$getCannonFlight();
        if (!flight.isFlying()) return;
        Vec3 vel = flight.getVelocity(partialTick);
        if (vel.lengthSqr() < 1.0E-4) return;

        float yaw = (float) (Mth.atan2(vel.z, vel.x) * Mth.RAD_TO_DEG) - 90;
        float pitch = (float) (Mth.atan2(vel.y, vel.horizontalDistance()) * Mth.RAD_TO_DEG);
        poseStack.translate(0, entity.getBbHeight() / 2 / scale, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch - 90));
        poseStack.translate(0, -0.9, 0);
        ci.cancel();
    }
}
