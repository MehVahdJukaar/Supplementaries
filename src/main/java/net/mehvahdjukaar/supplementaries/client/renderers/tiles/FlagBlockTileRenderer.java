package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.common.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.List;

public class FlagBlockTileRenderer implements BlockEntityRenderer<FlagBlockTile> {
    private final Minecraft minecraft = Minecraft.getInstance();

    public FlagBlockTileRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public void render(FlagBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        List<Pair<BannerPattern, DyeColor>> list = tile.getPatterns();
        if (list != null) {

            int lu = combinedLightIn & '\uffff';
            int lv = combinedLightIn >> 16 & '\uffff';

            int w = 24;
            int h = 16;

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0, 0.5);
            matrixStackIn.mulPose(RotHlpr.rot(tile.getDirection()));
            matrixStackIn.mulPose(RotHlpr.XN90);
            matrixStackIn.translate(0, 0, (1 / 16f));

            long time = tile.getLevel().getGameTime();

            double l = ClientConfigs.cached.FLAG_WAVELENGTH;
            long period = ClientConfigs.cached.FLAG_PERIOD;
            double wavyness = ClientConfigs.cached.FLAG_AMPLITUDE;
            double invdamping = ClientConfigs.cached.FLAG_AMPLITUDE_INCREMENT;

            BlockPos bp = tile.getBlockPos();
            //always from 0 to 1
            float t = ((float) Math.floorMod((long) (bp.getX() * 7 + bp.getZ() * 13) + time, period) + partialTicks) / ((float) period);


            int segmentLen = (minecraft.options.graphicsMode.getId() >= ClientConfigs.cached.FLAG_FANCINESS.ordinal()) ? 1 : w;
            for (int dX = 0; dX < w; dX += segmentLen) {

                float ang = (float) ((wavyness + invdamping * dX) * Mth.sin((float) ((((dX / l) - t * 2 * (float) Math.PI)))));

                renderPatterns(bufferIn, matrixStackIn, list, lu, lv, dX, w, h, segmentLen, ang);
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(ang));
                matrixStackIn.translate(0, 0, segmentLen / 16f);
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-ang));
            }

            matrixStackIn.popPose();
        }

    }

    public static void renderPatterns(PoseStack matrixStackIn, MultiBufferSource bufferIn, List<Pair<BannerPattern, DyeColor>> list, int combinedLightIn) {
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';
        renderPatterns(bufferIn, matrixStackIn, list, lu, lv, 0, 24, 16, 24, 0);
    }

    private static void renderPatterns(MultiBufferSource bufferIn, PoseStack matrixStackIn, List<Pair<BannerPattern, DyeColor>> list, int lu, int lv, int dX, int w, int h, int segmentlen, float ang) {

        for (int p = 0; p < list.size(); p++) {

            Material material = Materials.FLAG_MATERIALS.get(list.get(p).getFirst());
            VertexConsumer builder = material.buffer(bufferIn, p == 0 ? RenderType::entitySolid : RenderType::entityNoOutline);

            matrixStackIn.pushPose();

            float[] color = list.get(p).getSecond().getTextureDiffuseColors();
            float b = color[2];
            float g = color[1];
            float r = color[0];

            renderCurvedSegment(builder, material.sprite(), matrixStackIn, ang, dX, segmentlen, h, lu, lv, dX + segmentlen >= w, r, g, b);

            matrixStackIn.popPose();
        }
    }


    private static void renderCurvedSegment(VertexConsumer builder, TextureAtlasSprite sprite, PoseStack matrixStack, float angle, int dX,
                                            int length, int height, int lu, int lv, boolean end, float r, float g, float b) {

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

        float pU = RendererUtil.getRelativeU(sprite, maxU - (1/textW));
        float pV = RendererUtil.getRelativeV(sprite, maxV - w);
        float pV2 = RendererUtil.getRelativeV(sprite, w);

        maxU = RendererUtil.getRelativeU(sprite, maxU);
        u = RendererUtil.getRelativeU(sprite, u);
        maxV = RendererUtil.getRelativeV(sprite, maxV);
        v = RendererUtil.getRelativeV(sprite, v);

        Quaternion rotation = Vector3f.YP.rotationDegrees(angle);
        Quaternion rotation2 = Vector3f.YP.rotationDegrees(-angle);

        int nx = 1;
        int nz = 0;
        //0.4, 0.6

        //left
        matrixStack.pushPose();

        matrixStack.translate(hw, 0, 0);

        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, u, maxV, r, g, b, 1, lu, lv, nx, 0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, nx, 0, nz);

        matrixStack.mulPose(rotation);
        matrixStack.translate(0, 0, l);

        RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, nx, 0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, nx, 0, nz);

        matrixStack.popPose();

        //right
        matrixStack.pushPose();

        matrixStack.translate(-hw, 0, 0);

        RendererUtil.addVert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, -nx, 0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, u, maxV, r, g, b, 1, lu, lv, -nx, 0, nz);

        matrixStack.mulPose(rotation);
        matrixStack.translate(0, 0, l);

        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, -nx, 0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, -nx, 0, nz);

        matrixStack.popPose();

        //top
        matrixStack.pushPose();

        matrixStack.translate(hw, 0, 0);

        RendererUtil.addVert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, 0, 1, 0);
        matrixStack.translate(-w, 0, 0);
        RendererUtil.addVert(builder, matrixStack, 0, h, 0, u, pV2, r, g, b, 1, lu, lv, 0, 1, 0);

        matrixStack.mulPose(rotation);
        matrixStack.translate(0, 0, l);

        RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxU, pV2, r, g, b, 1, lu, lv, 0, 1, 0);
        matrixStack.mulPose(rotation2);
        matrixStack.translate(w, 0, 0);
        RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, 0, 1, 0);

        matrixStack.popPose();

        //bottom
        matrixStack.pushPose();

        matrixStack.translate(-hw, 0, 0);

        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, u, pV, r, g, b, 1, lu, lv, 0, -1, 0);
        matrixStack.translate(w, 0, 0);
        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, u, maxV, r, g, b, 1, lu, lv, 0, -1, 0);

        matrixStack.mulPose(rotation);
        matrixStack.translate(0, 0, l);

        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, 0, -1, 0);
        matrixStack.mulPose(rotation2);
        matrixStack.translate(-w, 0, 0);
        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxU, pV, r, g, b, 1, lu, lv, 0, -1, 0);


        matrixStack.popPose();

        //end
        if (end) {
            matrixStack.pushPose();

            matrixStack.mulPose(rotation);
            matrixStack.translate(0, 0, l);
            matrixStack.mulPose(rotation2);
            matrixStack.translate(-hw, 0, 0);

            RendererUtil.addVert(builder, matrixStack, 0, h, 0, pU, v, r, g, b, 1, lu, lv, 0, 0, 1);
            RendererUtil.addVert(builder, matrixStack, 0, 0, 0, pU, maxV, r, g, b, 1, lu, lv, 0, 0, 1);

            matrixStack.translate(w, 0, 0);

            RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, 0, 0, 1);
            RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, 0, 0, 1);

            matrixStack.popPose();
        }
    }


}