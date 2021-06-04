package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;

public class FlagBlockTileRenderer extends TileEntityRenderer<FlagBlockTile> {
    private final Minecraft minecraft = Minecraft.getInstance();
    public FlagBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }



    @Override
    public void render(FlagBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        List<Pair<BannerPattern, DyeColor>> list = tile.getPatterns();
        if (list != null) {

            int lu = combinedLightIn & '\uffff';
            int lv = combinedLightIn >> 16 & '\uffff';

            int w = 24;
            int h = 16;

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0, 0.5);
            matrixStackIn.mulPose(tile.getDirection().getRotation());
            matrixStackIn.mulPose(Const.XN90);
            matrixStackIn.translate(0, 0, (1/16f));

            long time = tile.getLevel().getGameTime();

            double l = ClientConfigs.cached.FLAG_WAVELENGTH;
            long period = ClientConfigs.cached.FLAG_PERIOD;
            double wavyness = ClientConfigs.cached.FLAG_AMPLITUDE;
            double invdamping = ClientConfigs.cached.FLAG_AMPLITUDE_INCREMENT;

            BlockPos bp = tile.getBlockPos();
            //alaways from 0 to 1
            float t = ((float) Math.floorMod((long) (bp.getX() * 7 + bp.getZ() * 13) + time, period) + partialTicks) / ((float)period);


            int segmentlen =  (minecraft.options.graphicsMode.getId() >= ClientConfigs.cached.FLAG_FANCINESS) ? 1 : w;
            for (int z = 0; z < w; z += segmentlen) {

                float ang = (float) ((wavyness + invdamping * z) * MathHelper.sin((float) ((((z / l) - t * 2 * (float) Math.PI)))));

                renderPatterns(bufferIn,matrixStackIn,list,lu,lv,z,w,h,segmentlen,ang);
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(ang));
                matrixStackIn.translate(0, 0, segmentlen / 16f);
                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-ang));
            }

            matrixStackIn.popPose();
        }

    }

    public static void renderPatterns(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,  List<Pair<BannerPattern, DyeColor>> list, int combinedLightIn) {
        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';
        renderPatterns(bufferIn,matrixStackIn,list,lu,lv,0,24,16,24,0);
    }

    private static void renderPatterns(IRenderTypeBuffer bufferIn, MatrixStack matrixStackIn, List<Pair<BannerPattern, DyeColor>> list, int lu, int lv, int z, int w, int h, int segmentlen, float ang){

        for(int p = 0; p<list.size(); p++) {
            ResourceLocation texture = FlagBlockTile.getFlagLocation(list.get(p).getFirst());
            RenderType renderType = p==0? RenderType.entitySolid(texture) : RenderType.entityNoOutline(texture);
            IVertexBuilder builder = bufferIn.getBuffer(renderType);

            matrixStackIn.pushPose();

            int color = list.get(p).getSecond().getColorValue();
            float b = (NativeImage.getR(color)) / 255f;
            float g = (NativeImage.getG(color)) / 255f;
            float r = (NativeImage.getB(color)) / 255f;


            renderCurvedSegment(builder, matrixStackIn, ang, z, segmentlen, h, lu, lv, z + segmentlen >= w, r, g, b);
            //IVertexBuilder builder2 = bufferIn.getBuffer(RenderType.getEntityNoOutline(new ResourceLocation("supplementaries:textures/entity/flagcross.png")));

            //renderCurvedSegment(builder2, matrixStackIn, ang, z, segmentlen, h, lu, lv, z + segmentlen >= w, zAxis);

            matrixStackIn.popPose();
        }


    }




    private static void renderCurvedSegment(IVertexBuilder builder, MatrixStack matrixStack, float angle, int posz, int lenght, int height, int lu, int lv, boolean end, float r, float g, float b) {
        float textw = 32f;

        float u = posz/textw;
        float v = 0;
        float maxv = height/16f;
        float maxu = u + lenght/textw;
        float w = 1/16f;
        float hw = w/2f;
        float l = lenght/16f;
        float h = height/16f;

        Quaternion rotation = Vector3f.YP.rotationDegrees(angle);
        Quaternion rotation2 = Vector3f.YP.rotationDegrees(-angle);

        int nx = 1;
        int nz = 0;
        //0.4, 0.6

        //left
        matrixStack.pushPose();

        matrixStack.translate(hw,0,0);

        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, u, maxv, r, g, b, 1, lu, lv, nx,0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, nx,0, nz);

        matrixStack.mulPose(rotation);
        matrixStack.translate(0, 0, l);

        RendererUtil.addVert(builder, matrixStack, 0, h, 0,maxu , v, r, g, b, 1, lu, lv, nx, 0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxu, maxv, r, g, b, 1, lu, lv, nx, 0, nz);

        matrixStack.popPose();

        //right
        matrixStack.pushPose();

        matrixStack.translate(-hw,0,0);

        RendererUtil.addVert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, -nx, 0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, u, maxv, r, g, b, 1, lu, lv, -nx, 0, nz);

        matrixStack.mulPose(rotation);
        matrixStack.translate(0, 0, l);

        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxu, maxv, r, g, b, 1, lu, lv, -nx, 0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxu, v, r, g, b, 1, lu, lv, -nx, 0, nz);

        matrixStack.popPose();

        //top
        matrixStack.pushPose();

        matrixStack.translate(hw,0,0);

        RendererUtil.addVert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lu, lv, 0, 1, 0);
        matrixStack.translate(-w,0,0);
        RendererUtil.addVert(builder, matrixStack, 0, h, 0, u, w, r, g, b, 1, lu, lv, 0, 1, 0);

        matrixStack.mulPose(rotation);
        matrixStack.translate(0, 0, l);

        RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxu, w, r, g, b, 1, lu, lv, 0, 1, 0);
        matrixStack.mulPose(rotation2);
        matrixStack.translate(w,0,0);
        RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxu, v, r, g, b, 1, lu, lv, 0, 1, 0);

        matrixStack.popPose();

        //bottom
        matrixStack.pushPose();

        matrixStack.translate(-hw,0,0);

        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, u, h-w, r, g, b, 1, lu, lv, -1, 0, 0);
        matrixStack.translate(w,0,0);
        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, u, h, r, g, b, 1, lu, lv, 0, -1, 0);

        matrixStack.mulPose(rotation);
        matrixStack.translate(0, 0, l);

        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxu, h, r, g, b, 1, lu, lv, 0, -1, 0);
        matrixStack.mulPose(rotation2);
        matrixStack.translate(-w,0,0);
        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxu, h-w, r, g, b, 1, lu, lv, 0, -1, 0);


        matrixStack.popPose();

        //end
        if(end) {
            matrixStack.pushPose();

            matrixStack.mulPose(rotation);
            matrixStack.translate(0, 0, l);
            matrixStack.mulPose(rotation2);
            matrixStack.translate(-hw, 0, 0);

            RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxu-(1/textw), 0, r, g, b, 1, lu, lv, 0, 0, 1);
            RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxu-(1/textw), maxv, r, g, b, 1, lu, lv, 0, 0, 1);

            matrixStack.translate(w, 0, 0);

            RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxu, maxv, r, g, b, 1, lu, lv, 0, 0, 1);
            RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxu, 0, r, g, b, 1, lu, lv, 0, 0, 1);

            matrixStack.popPose();
        }
    }


}