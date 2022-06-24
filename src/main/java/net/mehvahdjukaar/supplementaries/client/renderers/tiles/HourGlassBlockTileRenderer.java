package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.mehvahdjukaar.moonlight.client.renderUtils.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.HourGlassBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;


public class HourGlassBlockTileRenderer implements BlockEntityRenderer<HourGlassBlockTile> {

    public HourGlassBlockTileRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 48;
    }

    public static void renderSand(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                                  int combinedOverlayIn, TextureAtlasSprite sprite, float height, Direction dir) {

        VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());

        int color = 0xffffff;
        if (CommonUtil.FESTIVITY.isAprilsFool()) {
            color = ColorHelper.getRainbowColor(1);
            sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(Textures.WHITE_CONCRETE_TEXTURE);
        }


        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        Quaternion q = RotHlpr.rot(dir);
        matrixStackIn.mulPose(q);

        q = q.copy();
        q.conj();

        if (height != 0) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0, -0.25, 0);
            matrixStackIn.mulPose(q);
            matrixStackIn.translate(0, -0.125, 0);
            float h1 = height * 0.25f;
            RendererUtil.addCube(builder, matrixStackIn, 0.375f, 0.3125f, 0.25f, h1, sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                    true, true, true);
            if (dir == Direction.DOWN) {
                matrixStackIn.translate(0, -h1 - 0.25f, 0);
                RendererUtil.addCube(builder, matrixStackIn, 0.375f, 0.3125f, 0.0625f, h1 + 0.25f, sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                        true, true, false);
            }
            matrixStackIn.popPose();
        }
        if (height != 1) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(0, 0.25, 0);
            matrixStackIn.mulPose(q);
            matrixStackIn.translate(0, -0.125, 0);
            float h2 = (1 - height) * 0.25f;
            RendererUtil.addCube(builder, matrixStackIn, 0.375f, 0.3125f, 0.25f, h2, sprite, combinedLightIn, color, 1, combinedOverlayIn, true,
                    true, true, true);
            if (dir == Direction.UP) {
                matrixStackIn.translate(0, -h2 - 0.25, 0);
                RendererUtil.addCube(builder, matrixStackIn, 0.375f, 0.3125f, 0.0625f, h2 + 0.25f, sprite, combinedLightIn, color, 1f, combinedOverlayIn, true,
                        true, true, false);
            }
            matrixStackIn.popPose();
        }
        matrixStackIn.popPose();
    }

    @Override
    public void render(HourGlassBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        if (tile.sandType.isEmpty()) return;
        TextureAtlasSprite sprite = tile.getOrCreateSprite();

        float h = Mth.lerp(partialTicks, tile.prevProgress, tile.progress);
        Direction dir = tile.getBlockState().getValue(HourGlassBlock.FACING);

        renderSand(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, sprite, h, dir);
    }
}