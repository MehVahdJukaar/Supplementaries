package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.VertexUtils;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.HourGlassBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;


public class HourGlassBlockTileRenderer implements BlockEntityRenderer<HourGlassBlockTile> {

    public HourGlassBlockTileRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 48;
    }

    public static void renderSand(PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                                  ResourceLocation texture, float height, Direction dir) {

        int color = 0xffffff;
        if (MiscUtils.FESTIVITY.isAprilsFool()) {
            color = ColorHelper.getRainbowColor(1);
            texture = ModTextures.WHITE_CONCRETE_TEXTURE;
        }

        Material mat = ModMaterials.get(texture);
        VertexConsumer builder = mat.buffer(bufferIn, RenderType::entityTranslucentCull);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        Quaternionf q = dir .getRotation();

        poseStack.mulPose(q);

        q = new Quaternionf(q);
        q.conjugate();

        if (height != 0) {
            poseStack.pushPose();
            poseStack.translate(0, -0.25, 0);
            poseStack.mulPose(q);
            poseStack.translate(0, -0.125, 0);
            float h1 = height * 0.25f;
            VertexUtil.addCube(builder, poseStack, 0.375f, 0.3125f, 0.25f, h1,  combinedLightIn, color);
            if (dir == Direction.DOWN) {
                poseStack.translate(0, -h1 - 0.25f, 0);
                VertexUtil.addCube(builder, poseStack, 0.375f, 0.3125f, 0.0625f, h1 + 0.25f,  combinedLightIn, color,
                        1, false, false, false);
            }
            poseStack.popPose();
        }
        if (height != 1) {
            poseStack.pushPose();
            poseStack.translate(0, 0.25, 0);
            poseStack.mulPose(q);
            poseStack.translate(0, -0.125, 0);
            float h2 = (1 - height) * 0.25f;
            VertexUtil.addCube(builder, poseStack, 0.375f, 0.3125f, 0.25f, h2,  combinedLightIn, color);
            if (dir == Direction.UP) {
                poseStack.translate(0, -h2 - 0.25, 0);
                VertexUtil.addCube(builder, poseStack, 0.375f, 0.3125f, 0.0625f, h2 + 0.25f,  combinedLightIn, color,
                        1, false, false, false);
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public void render(HourGlassBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        if (tile.getSandData().isEmpty()) return;

        Direction dir = tile.getBlockState().getValue(HourGlassBlock.FACING);

        renderSand(poseStack, bufferIn, combinedLightIn, tile.getTexture(), tile.getProgress(partialTicks), dir);
    }
}