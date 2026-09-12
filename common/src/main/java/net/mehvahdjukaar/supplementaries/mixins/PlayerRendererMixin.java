package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonRiderRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.PicklePlayer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
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
}
