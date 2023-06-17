package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.supplementaries.common.block.IBellConnections;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BellRenderer.class)
public abstract class BellRendererMixin {

    @Inject(method = "render*", at = @At("HEAD"))
    public void render(BellBlockEntity tile, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, CallbackInfo info) {
        if (tile instanceof IBellConnections connections) {
            ResourceLocation model = switch (connections.getConnected()) {
                case ROPE -> ClientRegistry.BELL_ROPE;
                case CHAIN -> ClientRegistry.BELL_CHAIN;
                default -> null;
            };
            if (model != null) {
                int light = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().below());
                RenderUtil.renderModel(model, matrixStackIn, bufferIn, Minecraft.getInstance().getBlockRenderer(),
                        light, combinedOverlayIn, true);
            }
        }
    }
}
