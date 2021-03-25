package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.LaserBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;


public class LaserBlockTileRenderer extends TileEntityRenderer<LaserBlockTile> {
    public LaserBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(LaserBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        if (tile.canEmit()) {
            int MAXLENGHT = tile.MAXLENGHT;

            int lenght = tile.lenght;
            if (lenght == 0)
                return;
            TextureAtlasSprite sprite_o = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(Textures.LASER_OVERLAY_TEXTURE);
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(Textures.LASER_BEAM_TEXTURE);
            // IVertexBuilder builder = bufferIn.getBuffer(RenderType.getTranslucent());


            // IVertexBuilder builder
            // =bufferIn.getBuffer(Customrender.CustomRenderTypes.TRANSLUCENT_CUSTOM);
            int color = 0xff00ff;
            Direction dir = tile.getDirection();
            float yaw = dir.toYRot();
            float pitch = 0;
            if (dir == Direction.UP)
                pitch = 90f;
            else if (dir == Direction.DOWN)
                pitch = -90f;
            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);
            matrixStackIn.mulPose(dir.getRotation());
            matrixStackIn.translate(0, -0.5, 0);
            int j = 240;
            int k = combinedLightIn >> 16 & 255;
            combinedLightIn = j | k << 16;
            float w2 = 0.0625f - 0.015625f;
            float w = 0.125f;
            if (!Minecraft.getInstance().isPaused()) {
                float d = 0.015625f * MathHelper.lerp(partialTicks, tile.prevWidth, tile.width);
                w += d / 2;
                w2 += d / 1.5;
            }
            // matrixStackIn.translate(0, 1, 0);
            int l = Math.min(lenght, MAXLENGHT);
            for (int i = 0; i < l; i++) {
                matrixStackIn.translate(0, 1, 0);
                matrixStackIn.pushPose();
                IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.lightning());
                RendererUtil.addCube(builder1, matrixStackIn, w, 1f, sprite, combinedLightIn, color, 0.7f, combinedOverlayIn, false, false, false,
                        false);
                RendererUtil.addCube(builder1, matrixStackIn, w2, 1f, sprite, combinedLightIn, 0xFFFFFF, 1f, combinedOverlayIn, false, false,
                        false, false);
                matrixStackIn.popPose();
                matrixStackIn.pushPose();
                IVertexBuilder builder = bufferIn.getBuffer(RenderType.translucentMovingBlock());
                //RendererUtil.addCube(builder, matrixStackIn, 0.25f, 1f, sprite_o, combinedLightIn, 0xFFFFFF, 1f, combinedOverlayIn, false, false, false, false);
                matrixStackIn.popPose();

            }
            if (lenght == MAXLENGHT + 1) {
                matrixStackIn.translate(0, 1, 0);
                matrixStackIn.pushPose();
                IVertexBuilder builder1 = bufferIn.getBuffer(RenderType.lightning());
                TextureAtlasSprite sprite1 = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(Textures.LASER_BEAM_END_TEXTURE);
                RendererUtil.addCube(builder1, matrixStackIn, w, 1f, sprite1, combinedLightIn, color, 0.7f, combinedOverlayIn, false, false, false,
                        true);
                RendererUtil.addCube(builder1, matrixStackIn, w2, 1f, sprite1, combinedLightIn, 0xFFFFFF, 1f, combinedOverlayIn, false, false,
                        false, true);
                matrixStackIn.popPose();

            }
            matrixStackIn.popPose();
            matrixStackIn.pushPose();

            matrixStackIn.popPose();
        }
    }
}
