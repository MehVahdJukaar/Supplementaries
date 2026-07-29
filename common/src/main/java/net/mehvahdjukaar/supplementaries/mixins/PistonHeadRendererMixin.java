package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ICarryingMovingPiston;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PistonCooperationData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonHeadRenderer.class)
public class PistonHeadRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void supp$renderCarriedBlockEntity(PistonMovingBlockEntity piston, float partialTick,
                                               PoseStack poseStack, MultiBufferSource bufferSource,
                                               int packedLight, int packedOverlay, CallbackInfo ci) {
        if (!(piston instanceof ICarryingMovingPiston carrying)) return;
        if (piston.getProgress(partialTick) > 1.0F) return;
        // Quark's module owns piston moves, so it holds the data and its own renderer hook draws
        // the carried block. Bail out instead of drawing an empty default one over it. A pulley
        // move always carries our own NBT, so it still renders here.
        if (carrying.supp$getCarriedBlockEntityNbt() == null
                && PistonCooperationData.blockEntityMovesHandledByQuark()) return;

        BlockEntity carriedBE = carrying.supp$getOrCreateCachedCarriedBlockEntity();
        if (carriedBE == null) return;

        BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance()
                .getBlockEntityRenderDispatcher().getRenderer(carriedBE);
        if (renderer == null) return;

        poseStack.pushPose();
        poseStack.translate(piston.getXOff(partialTick), piston.getYOff(partialTick), piston.getZOff(partialTick));
        renderer.render(carriedBE, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();

        // For non-MODEL render shapes (e.g. chests = ENTITYBLOCK_ANIMATED), the block
        // model would draw nothing anyway — skip vanilla rendering.
        if (piston.getMovedState().getRenderShape() != RenderShape.MODEL) {
            ci.cancel();
        }
    }
}
