package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;

import java.util.List;

import static net.mehvahdjukaar.supplementaries.client.ModMaterials.FLAG_BASE_MATERIAL;

public class FlagBlockTileRenderer implements BlockEntityRenderer<FlagBlockTile> {
    private final Minecraft minecraft = Minecraft.getInstance();
    private static ModelPart flag;

    public FlagBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelpart = context.bakeLayer(ModelLayers.BANNER);
        flag = modelpart.getChild("flag");
    }

    @ForgeOverride
    public AABB getRenderBoundingBox(FlagBlockTile tile) {
        Direction dir = tile.getDirection();
        return new AABB(0.25, 0, 0.25, 0.75, 1, 0.75).expandTowards(
                dir.getStepX() * 1.35f, 0, dir.getStepZ() * 1.35f).move(tile.getBlockPos());
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    public static void renderBanner(float ang, PoseStack matrixStack, MultiBufferSource bufferSource, int light, int pPackedOverlay,
                              BannerPatternLayers patterns, DyeColor baseColor) {
        matrixStack.pushPose();
        matrixStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        matrixStack.mulPose(Axis.YP.rotationDegrees(0.05f * ang));
        flag.xRot = (float) (0.5 * Math.PI);
        flag.yRot = (float) (1 * Math.PI);
        flag.zRot = (float) (0.5 * Math.PI);
        flag.y = -12;
        flag.x = 1.5f;
        BannerRenderer.renderPatterns(matrixStack, bufferSource, light, pPackedOverlay, flag, ModelBakery.BANNER_BASE,
                true, baseColor, patterns);
        matrixStack.popPose();
    }

    @Override
    public void render(FlagBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        BannerPatternLayers patterns = tile.getPatterns();


        int lu = VertexUtil.lightU(combinedLightIn);
        int lv = VertexUtil.lightV(combinedLightIn);

        int w = 24;
        int h = 16;

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(RotHlpr.rot(tile.getDirection().getOpposite()));
        poseStack.translate(0, 0, (1 / 16f));

        long time = tile.getLevel().getGameTime();

        double l = ClientConfigs.Blocks.FLAG_WAVELENGTH.get();
        long period = (ClientConfigs.Blocks.FLAG_PERIOD.get());
        double wavyness = ClientConfigs.Blocks.FLAG_AMPLITUDE.get();
        double invdamping = ClientConfigs.Blocks.FLAG_AMPLITUDE_INCREMENT.get();

        BlockPos bp = tile.getBlockPos();
        //always from 0 to 1

        float t = ((float) Math.floorMod(bp.getX() * 7L + bp.getZ() * 13L + time, period) + partialTicks) / ((float) period);

        DyeColor color = tile.getColor();
        if (ClientConfigs.Blocks.FLAG_BANNER.get()) {
            float ang = (float) ((wavyness + invdamping * w) * Mth.sin((float) (((w / l) - t * 2 * (float) Math.PI))));
            renderBanner(ang, poseStack, bufferIn, combinedLightIn, combinedOverlayIn, patterns, color);
        } else {

            int segmentLen = (minecraft.options.graphicsMode().get().getId()) >= ClientConfigs.Blocks.FLAG_FANCINESS.get().ordinal() ? 1 : w;
            float oldAng = 0;
            for (int dX = 0; dX < w; dX += segmentLen) {

                float ang = (float) ((wavyness + invdamping * dX) * Mth.sin((float) ((dX / l) - t * 2 * (float) Math.PI)));

                renderPatterns(bufferIn, poseStack, patterns, lu, lv, dX, w, h, segmentLen,
                        ang, oldAng, color);
                poseStack.mulPose(Axis.YP.rotationDegrees(ang));
                poseStack.translate(0, 0, segmentLen / 16f);
                poseStack.mulPose(Axis.YP.rotationDegrees(-ang));
                oldAng = ang;
            }
        }

        poseStack.popPose();
    }

    public static void renderPatterns(PoseStack matrixStackIn, MultiBufferSource bufferIn, BannerPatternLayers patterns,
                                      int combinedLightIn, DyeColor baseColor) {
        int lu = VertexUtil.lightU(combinedLightIn);
        int lv = VertexUtil.lightV(combinedLightIn);

        renderPatterns(bufferIn, matrixStackIn, patterns, lu, lv, 0, 24, 16, 24,
                0, 0, baseColor);
    }


    private static void renderPatterns(MultiBufferSource bufferIn, PoseStack poseStack, BannerPatternLayers list,
                                       int lu, int lv, int dX, int w, int h, int segmentLen,
                                       float ang, float oldAng, DyeColor baseColor) {

        renderLayer(bufferIn, poseStack, lu, lv, dX, w, h, segmentLen, ang, oldAng, FLAG_BASE_MATERIAL, true, baseColor);

        for (BannerPatternLayers.Layer layer : list.layers()) {
            Material material = ModMaterials.FLAG_MATERIALS.apply(layer.pattern().value());
            DyeColor dyeColor = layer.color();
            renderLayer(bufferIn, poseStack, lu, lv, dX, w, h, segmentLen, ang, oldAng, material, false, dyeColor);
        }
    }

    private static void renderLayer(MultiBufferSource bufferIn, PoseStack matrixStackIn, int lu, int lv, int dX, int w, int h,
                                    int segmentlen, float ang, float oldAng, Material material,
                                    boolean solid, DyeColor dyeColor) {
        if (material == null) {
            return;
        }
        VertexConsumer builder = material.buffer(bufferIn, solid ? RenderType::entitySolid : RenderType::entityNoOutline);

        matrixStackIn.pushPose();

        int color = dyeColor.getTextureDiffuseColor();
        float b = FastColor.ARGB32.blue(color) / 255f;
        float g = FastColor.ARGB32.green(color) / 255f;
        float r = FastColor.ARGB32.red(color) / 255f;

        renderCurvedSegment(builder, matrixStackIn, ang, oldAng, dX, segmentlen, h, lu, lv, dX + segmentlen >= w, r, g, b);

        matrixStackIn.popPose();
    }


    private static void renderCurvedSegment(VertexConsumer builder, PoseStack matrixStack, float angle, float oldAng, int dX,
                                            int length, int height, int lu, int lv, boolean end,
                                            float r, float g, float b) {

        float textW = 32f;
        float textH = 16f;

        float u = dX / textW;
        float v = 0;
        float maxV = v + height / textH;
        float maxU = u + length / textW;
        float w = 1 / 16f;
        float hw = w / 2f;
        float l = length / 16f;
        float h = height / 16f;

        float pU = maxU - (1 / textW);
        float pV = maxV - w;
        float pV2 = w;

        //TODO: fix
        Quaternionf rot = Axis.YP.rotationDegrees(angle);
        Quaternionf oldRot = Axis.YP.rotationDegrees(oldAng);
        Quaternionf rotInc = Axis.YP.rotationDegrees(angle - oldAng);
        Quaternionf rotInv = Axis.YP.rotationDegrees(-angle);

        //correct
        int nx = 1;
        int nz = 0;
        int ny = 0;
        //0.4, 0.6

        //left
        matrixStack.pushPose();

        matrixStack.translate(hw, 0, 0);

        matrixStack.mulPose(oldRot);

        VertexUtil.vert(builder, matrixStack, 0, 0, 0, u, maxV, r, g, b, 1, lu, lv, nx, ny, nz);
        VertexUtil.vert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, nx, ny, nz);

        //still slightly off but better than before
        matrixStack.mulPose(rotInc);
        matrixStack.translate(0, 0, l);


        VertexUtil.vert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, nx, ny, nz);
        VertexUtil.vert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, nx, ny, nz);

        matrixStack.popPose();

        //right
        matrixStack.pushPose();

        matrixStack.translate(-hw, 0, 0);
        matrixStack.mulPose(oldRot);

        VertexUtil.vert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, -nx, ny, nz);
        VertexUtil.vert(builder, matrixStack, 0, 0, 0, u, maxV, r, g, b, 1, lu, lv, -nx, ny, nz);

        matrixStack.mulPose(rotInc);
        matrixStack.translate(0, 0, l);

        VertexUtil.vert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, -nx, ny, nz);
        VertexUtil.vert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, -nx, ny, nz);

        matrixStack.popPose();

        //top
        matrixStack.pushPose();

        matrixStack.translate(hw, 0, 0);

        VertexUtil.vert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, 0, 1, 0);
        matrixStack.translate(-w, 0, 0);
        VertexUtil.vert(builder, matrixStack, 0, h, 0, u, pV2, r, g, b, 1, lu, lv, 0, 1, 0);

        matrixStack.mulPose(rot);
        matrixStack.translate(0, 0, l);

        VertexUtil.vert(builder, matrixStack, 0, h, 0, maxU, pV2, r, g, b, 1, lu, lv, 0, 1, 0);
        matrixStack.mulPose(rotInv);
        matrixStack.translate(w, 0, 0);
        VertexUtil.vert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, 0, 1, 0);

        matrixStack.popPose();

        //bottom
        matrixStack.pushPose();

        matrixStack.translate(-hw, 0, 0);

        VertexUtil.vert(builder, matrixStack, 0, 0, 0, u, pV, r, g, b, 1, lu, lv, 0, -1, 0);
        matrixStack.translate(w, 0, 0);
        VertexUtil.vert(builder, matrixStack, 0, 0, 0, u, maxV, r, g, b, 1, lu, lv, 0, -1, 0);

        matrixStack.mulPose(rot);
        matrixStack.translate(0, 0, l);

        VertexUtil.vert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, 0, -1, 0);
        matrixStack.mulPose(rotInv);
        matrixStack.translate(-w, 0, 0);
        VertexUtil.vert(builder, matrixStack, 0, 0, 0, maxU, pV, r, g, b, 1, lu, lv, 0, -1, 0);


        matrixStack.popPose();

        //end
        if (end) {
            matrixStack.pushPose();

            matrixStack.mulPose(rot);
            matrixStack.translate(0, 0, l);
            matrixStack.mulPose(rotInv);
            matrixStack.translate(-hw, 0, 0);

            VertexUtil.vert(builder, matrixStack, 0, h, 0, pU, v, r, g, b, 1, lu, lv, 0, 0, 1);
            VertexUtil.vert(builder, matrixStack, 0, 0, 0, pU, maxV, r, g, b, 1, lu, lv, 0, 0, 1);

            matrixStack.translate(w, 0, 0);

            VertexUtil.vert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, 0, 0, 1);
            VertexUtil.vert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, 0, 0, 1);

            matrixStack.popPose();
        }
    }

}