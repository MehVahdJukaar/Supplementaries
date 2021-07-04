package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;


public class FaucetBlockTileRenderer extends TileEntityRenderer<FaucetBlockTile> {
    private final Minecraft minecraft = Minecraft.getInstance();
    public FaucetBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FaucetBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int light,
                       int ov) {
        if (tile.hasWater() && tile.isOpen() && !tile.isConnectedBelow() && !CommonUtil.FESTIVITY.isEarthDay()) {
            ResourceLocation texture = tile.fluidHolder.getFluid().getFlowingTexture();
            TextureAtlasSprite sprite = minecraft.getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(texture);
            IVertexBuilder builder = bufferIn.getBuffer(RenderType.translucentMovingBlock());
            int color = tile.fluidHolder.getFlowingTint(tile.getLevel(),tile.getBlockPos());
            int luminosity = tile.fluidHolder.getFluid().getLuminosity();
            if(luminosity!=0) light = light & 15728640 | luminosity << 4;
            float opacity = 1.3f;
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.25, 0.5);
            matrixStackIn.scale(2f,2f,2f);
            float h = 0.5f/16f;
            for(int i = 0; i<16; i++) {
                opacity = Math.min(1,opacity-0.0082f*i);
                RendererUtil.addCube(builder, matrixStackIn,0,i*h, 0.125f, h, sprite, light, color, opacity, ov, false, false, true, false,true);
                matrixStackIn.translate(0,-h,0);
            }
            matrixStackIn.popPose();
        }
    }
}