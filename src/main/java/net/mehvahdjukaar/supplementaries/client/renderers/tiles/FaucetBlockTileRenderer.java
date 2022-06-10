package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;


public class FaucetBlockTileRenderer implements BlockEntityRenderer<FaucetBlockTile> {
    private final Minecraft minecraft = Minecraft.getInstance();

    public FaucetBlockTileRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 80;
    }

    @Override
    public void render(FaucetBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int light, int ov) {
        if (tile.hasWater() && tile.isOpen() && !tile.isConnectedBelow() && !CommonUtil.FESTIVITY.isEarthDay()) {
            ResourceLocation texture = tile.tempFluidHolder.getFluid().get().getFlowingTexture();
            TextureAtlasSprite sprite = minecraft.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
            VertexConsumer builder = bufferIn.getBuffer(RenderType.translucentMovingBlock());
            int color = tile.tempFluidHolder.getFlowingTint(tile.getLevel(), tile.getBlockPos());
            int luminosity = tile.tempFluidHolder.getFluid().get().getLuminosity();
            if (luminosity != 0) light = light & 15728640 | luminosity << 4;
            float opacity = 1.3f;
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.25, 0.5);
            matrixStackIn.scale(2f, 2f, 2f);
            float h = 0.5f / 16f;
            for (int i = 0; i < 16; i++) {
                opacity = Math.min(1, opacity - 0.0082f * i);
                RendererUtil.addCube(builder, matrixStackIn, 0, i * h, 0.125f, h, sprite, light, color, opacity, false, false, true, false, true);
                matrixStackIn.translate(0, -h, 0);
            }
            matrixStackIn.popPose();
        }
    }
}