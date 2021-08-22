package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.HourGlassBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.color.HSLColor;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
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
import net.minecraft.util.math.vector.Quaternion;


public class HourGlassBlockTileRenderer extends TileEntityRenderer<HourGlassBlockTile> {

    public HourGlassBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }


    public static void renderSand(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                                   int combinedOverlayIn, TextureAtlasSprite sprite, float height, Direction dir){

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.translucent());

        int color = 0xffffff;
        if(CommonUtil.FESTIVITY.isAprilsFool()){
            color = HSLColor.getRainbowColor(1);
            sprite = Minecraft.getInstance().getTextureAtlas(AtlasTexture.LOCATION_BLOCKS).apply(Textures.WHITE_CONCRETE_TEXTURE);
        }


        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5,0.5,0.5);
        Quaternion q = Const.rot(dir);
        matrixStackIn.mulPose(q);

        q.conj();

        if(height!=0) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0,-0.25,0);
            matrixStackIn.mulPose(q);
            matrixStackIn.translate(0,-0.125,0);
            float h1 = height * 0.25f;
            RendererUtil.addCube(builder, matrixStackIn, 0.375f,0.3125f, 0.25f, h1, sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                    true, true, true);
            if(dir==Direction.DOWN) {
                matrixStackIn.translate(0, -h1 - 0.25f, 0);
                RendererUtil.addCube(builder, matrixStackIn,0.375f,0.3125f, 0.0625f, h1 + 0.25f, sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                        true, true, false);
            }
            matrixStackIn.popPose();
        }
        if(height!=1) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0,0.25,0);
            matrixStackIn.mulPose(q);
            matrixStackIn.translate(0,-0.125,0);
            float h2 = (1 - height) * 0.25f;
            RendererUtil.addCube(builder, matrixStackIn,0.375f,0.3125f, 0.25f, h2 , sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                    true, true, true);
            if(dir==Direction.UP) {
                matrixStackIn.translate(0, -h2 -0.25, 0);
                RendererUtil.addCube(builder, matrixStackIn,0.375f,0.3125f, 0.0625f, h2 + 0.25f, sprite, combinedLightIn, color, 1f, combinedOverlayIn, true,
                        true, true, false);
            }
            matrixStackIn.popPose();
        }
        matrixStackIn.popPose();
    }

    @Override
    public void render(HourGlassBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        if(tile.sandType.isEmpty())return;
        TextureAtlasSprite sprite = tile.getOrCreateSprite();

        float h = MathHelper.lerp(partialTicks, tile.prevProgress, tile.progress);
        Direction dir = tile.getBlockState().getValue(HourGlassBlock.FACING);

        renderSand(matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn,sprite,h,dir);
    }
}