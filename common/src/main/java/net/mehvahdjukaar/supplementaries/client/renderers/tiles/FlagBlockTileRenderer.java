package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.renderers.VertexUtils;
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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

public class FlagBlockTileRenderer implements BlockEntityRenderer<FlagBlockTile> {
    private final Minecraft minecraft = Minecraft.getInstance();
    private final ModelPart flag;

    public FlagBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart modelpart = context.bakeLayer(ModelLayers.BANNER);
        this.flag = modelpart.getChild("flag");
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    private void renderBanner(float ang, PoseStack matrixStack, MultiBufferSource bufferSource, int light, int pPackedOverlay, List<Pair<Holder<BannerPattern>, DyeColor>> list) {
        matrixStack.pushPose();
        matrixStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        matrixStack.mulPose(Axis.YP.rotationDegrees(0.05f * ang));
        this.flag.xRot = (float) (0.5 * Math.PI);
        this.flag.yRot = (float) (1 * Math.PI);
        this.flag.zRot = (float) (0.5 * Math.PI);
        this.flag.y = -12;
        this.flag.x = 1.5f;
        BannerRenderer.renderPatterns(matrixStack, bufferSource, light, pPackedOverlay, this.flag, ModelBakery.BANNER_BASE, true, list);
        matrixStack.popPose();
    }

    @Override
    public void render(FlagBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        List<Pair<Holder<BannerPattern>, DyeColor>> list = tile.getPatterns();

        if (list != null) {

            int lu = combinedLightIn & '\uffff';
            int lv = combinedLightIn >> 16 & '\uffff';

            int w = 24;
            int h = 16;

            poseStack.pushPose();
            poseStack.translate(0.5, 0, 0.5);
            poseStack.mulPose(RotHlpr.rot(tile.getDirection()));
            poseStack.scale(1,-1,-1);
            poseStack.translate(0, 0, (1 / 16f));

            long time = tile.getLevel().getGameTime();

            double l = ClientConfigs.Blocks.FLAG_WAVELENGTH.get();
            long  period =  (ClientConfigs.Blocks.FLAG_PERIOD.get());
            double wavyness = ClientConfigs.Blocks.FLAG_AMPLITUDE.get();
            double invdamping = ClientConfigs.Blocks.FLAG_AMPLITUDE_INCREMENT.get();

            BlockPos bp = tile.getBlockPos();
            //always from 0 to 1
            //TODO: fix
            float t = ((float) Math.floorMod(bp.getX() * 7L + bp.getZ() * 13L + time, period) + partialTicks) / ((float) period);

            if (ClientConfigs.Blocks.FLAG_BANNER.get()) {
                float ang = (float) ((wavyness + invdamping * w) * Mth.sin((float) (((w / l) - t * 2 * (float) Math.PI))));
                renderBanner(ang, poseStack, bufferIn, combinedLightIn, combinedOverlayIn, list);
            } else {

                int segmentLen = (minecraft.options.graphicsMode().get().getId()) >= ClientConfigs.Blocks.FLAG_FANCINESS.get().ordinal() ? 1 : w;
                float oldAng = 0;
                for (int dX = 0; dX < w; dX += segmentLen) {

                    float ang = (float) ((wavyness + invdamping * dX) * Mth.sin((float) ((dX / l) - t * 2 * (float) Math.PI)));

                    renderPatterns(bufferIn, poseStack, list, lu, lv, dX, w, h, segmentLen,ang, oldAng);
                    poseStack.mulPose(Axis.YP.rotationDegrees(ang));
                    poseStack.translate(0, 0, segmentLen / 16f);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-ang));
                    oldAng = ang;
                }
            }

            poseStack.popPose();
        }

    }

    public static void renderPatterns(PoseStack matrixStackIn, MultiBufferSource bufferIn, List<Pair<Holder<BannerPattern>, DyeColor>> list, int combinedLightIn) {
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';
        renderPatterns(bufferIn, matrixStackIn, list, lu, lv, 0, 24, 16, 24, 0,0);
    }


    private static void renderPatterns(MultiBufferSource bufferIn, PoseStack matrixStackIn, List<Pair<Holder<BannerPattern>, DyeColor>> list,
                                       int lu, int lv, int dX, int w, int h, int segmentlen, float ang, float oldAng) {

        for (int p = 0; p < list.size(); p++) {

            Material material = ModMaterials.FLAG_MATERIALS.get().get(list.get(p).getFirst().value());
            if(material == null){
                continue;
            }
            VertexConsumer builder = material.buffer(bufferIn, p == 0 ? RenderType::entitySolid : RenderType::entityNoOutline);

            matrixStackIn.pushPose();

            float[] color = list.get(p).getSecond().getTextureDiffuseColors();
            float b = color[2];
            float g = color[1];
            float r = color[0];

            renderCurvedSegment(builder, matrixStackIn, ang, oldAng, dX, segmentlen, h, lu, lv, dX + segmentlen >= w, r, g, b);

            matrixStackIn.popPose();
        }
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
        float pV =  maxV - w;
        float pV2 =  w;

        //TODO: fix
        Quaternionf rot = Axis.YP.rotationDegrees(angle);
        Quaternionf oldRot = Axis.YP.rotationDegrees(oldAng);
        Quaternionf rotInc = Axis.YP.rotationDegrees(angle-oldAng);
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

        VertexUtils.vert(builder, matrixStack, 0, 0, 0, u, maxV, r, g, b, 1, lu, lv, nx, ny, nz);
        VertexUtils.vert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, nx, ny, nz);

        //still slightly off but better than before
        matrixStack.mulPose(rotInc);
        matrixStack.translate(0, 0, l);


        VertexUtils.vert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, nx, ny, nz);
        VertexUtils.vert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, nx, ny, nz);

        matrixStack.popPose();

        //right
        matrixStack.pushPose();

        matrixStack.translate(-hw, 0, 0);
        matrixStack.mulPose(oldRot);

        VertexUtils.vert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, -nx, ny, nz);
        VertexUtils.vert(builder, matrixStack, 0, 0, 0, u, maxV, r, g, b, 1, lu, lv, -nx, ny, nz);

        matrixStack.mulPose(rotInc);
        matrixStack.translate(0, 0, l);

        VertexUtils.vert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, -nx, ny, nz);
        VertexUtils.vert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, -nx, ny, nz);

        matrixStack.popPose();

        //top
        matrixStack.pushPose();

        matrixStack.translate(hw, 0, 0);

        VertexUtils.vert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, 0, 1, 0);
        matrixStack.translate(-w, 0, 0);
        VertexUtils.vert(builder, matrixStack, 0, h, 0, u, pV2, r, g, b, 1, lu, lv, 0, 1, 0);

        matrixStack.mulPose(rot);
        matrixStack.translate(0, 0, l);

        VertexUtils.vert(builder, matrixStack, 0, h, 0, maxU, pV2, r, g, b, 1, lu, lv, 0, 1, 0);
        matrixStack.mulPose(rotInv);
        matrixStack.translate(w, 0, 0);
        VertexUtils.vert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, 0, 1, 0);

        matrixStack.popPose();

        //bottom
        matrixStack.pushPose();

        matrixStack.translate(-hw, 0, 0);

        VertexUtils.vert(builder, matrixStack, 0, 0, 0, u, pV, r, g, b, 1, lu, lv, 0, -1, 0);
        matrixStack.translate(w, 0, 0);
        VertexUtils.vert(builder, matrixStack, 0, 0, 0, u, maxV, r, g, b, 1, lu, lv, 0, -1, 0);

        matrixStack.mulPose(rot);
        matrixStack.translate(0, 0, l);

        VertexUtils.vert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, 0, -1, 0);
        matrixStack.mulPose(rotInv);
        matrixStack.translate(-w, 0, 0);
        VertexUtils.vert(builder, matrixStack, 0, 0, 0, maxU, pV, r, g, b, 1, lu, lv, 0, -1, 0);


        matrixStack.popPose();

        //end
        if (end) {
            matrixStack.pushPose();

            matrixStack.mulPose(rot);
            matrixStack.translate(0, 0, l);
            matrixStack.mulPose(rotInv);
            matrixStack.translate(-hw, 0, 0);

            VertexUtils.vert(builder, matrixStack, 0, h, 0, pU, v, r, g, b, 1, lu, lv, 0, 0, 1);
            VertexUtils.vert(builder, matrixStack, 0, 0, 0, pU, maxV, r, g, b, 1, lu, lv, 0, 0, 1);

            matrixStack.translate(w, 0, 0);

            VertexUtils.vert(builder, matrixStack, 0, 0, 0, maxU, maxV, r, g, b, 1, lu, lv, 0, 0, 1);
            VertexUtils.vert(builder, matrixStack, 0, h, 0, maxU, v, r, g, b, 1, lu, lv, 0, 0, 1);

            matrixStack.popPose();
        }
    }

}