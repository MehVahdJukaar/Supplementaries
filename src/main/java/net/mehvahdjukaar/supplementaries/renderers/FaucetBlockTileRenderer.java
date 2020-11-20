package net.mehvahdjukaar.supplementaries.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.blocks.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FaucetBlockTileRenderer extends TileEntityRenderer<FaucetBlockTile> {
    public FaucetBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FaucetBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        if (tile.hasWater() && tile.isOpen() && !tile.hasJar()) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(CommonUtil.FAUCET_TEXTURE);
            // TODO:remove breaking animation
            IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucentMovingBlock());
            int color = tile.watercolor;
            if(color==-1)color = tile.updateClientWaterColor();
            float opacity = 0.75f;
            matrixStackIn.push();
            matrixStackIn.translate(0.5, -0.5 - 0.1875, 0.5);

            RendererUtil.addCube(builder, matrixStackIn, 0.25f, 1, sprite, combinedLightIn, color, opacity, combinedOverlayIn, false,
                    false, true, false);

            matrixStackIn.pop();
        }
    }
}