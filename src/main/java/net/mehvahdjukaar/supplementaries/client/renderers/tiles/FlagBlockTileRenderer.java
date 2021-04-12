package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.block.tiles.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.DyeColor;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;

public class FlagBlockTileRenderer extends TileEntityRenderer<FlagBlockTile> {
    public FlagBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }





    //render(FlagBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
     //      int combinedOverlayIn) {

    public void render2(FlagBlockTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {
        List<Pair<BannerPattern, DyeColor>> list = tile.getPatterns();
        if (list != null) {

            matrixStack.pushPose();
            long time = 0;

            if(tile.hasLevel()){
                time = tile.getLevel().getGameTime();
                matrixStack.translate(0.5,0,0.5);
                matrixStack.mulPose(tile.getDirection().getRotation());
                matrixStack.mulPose(Const.XN90) ;
            }

            matrixStack.pushPose();
            IVertexBuilder ivertexbuilder = ModelBakery.BANNER_BASE.buffer(buffer, RenderType::entitySolid);

            BlockPos blockpos = tile.getBlockPos();
            float blockOffset = ((float)Math.floorMod((long)(blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + time, 100L) + partialTicks) / 100.0F;
            //this.flag.xRot = (-0.0125F + 0.01F * MathHelper.cos(((float)Math.PI * 2F) * blockOffset)) * (float)Math.PI;

            //renderPatterns(matrixStack, buffer, light, overlay, this.flag, ModelBakery.BANNER_BASE, true, list);
            matrixStack.popPose();
            matrixStack.popPose();
        }
    }


    public static void renderPatterns(MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, ModelRenderer model, RenderMaterial material, List<Pair<BannerPattern, DyeColor>> patternsList, boolean hasFoil) {
        model.render(matrixStack, material.buffer(buffer, RenderType::entitySolid, hasFoil), light, overlay);

        for(int i = 0; i < 17 && i < patternsList.size(); ++i) {
            Pair<BannerPattern, DyeColor> pair = patternsList.get(i);
            float[] afloat = pair.getSecond().getTextureDiffuseColors();
            RenderMaterial rendermaterial = new RenderMaterial(Atlases.BANNER_SHEET, FlagBlockTile.getFlagLocation(pair.getFirst()));
            model.render(matrixStack, rendermaterial.buffer(buffer, RenderType::entityNoOutline), light, overlay, afloat[0], afloat[1], afloat[2], 1.0F);
        }

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
            matrixStackIn.translate(0, 0, -1+(1/16f));

            long time = tile.getLevel().getGameTime();

            double l = ClientConfigs.block.FLAG_WAVELENGTH.get();
            double speed = ClientConfigs.block.FLAG_SPEED.get();
            double wavyness = ClientConfigs.block.FLAG_AMPLITUDE.get();
            double invdamping = ClientConfigs.block.FLAG_AMPLITUDE_INCREMENT.get();

            BlockPos blockpos = tile.getBlockPos();
            float period = ((float) Math.floorMod((long) (blockpos.getX() * 7 + blockpos.getZ() * 13) + time, 100L) + partialTicks) / 100.0F;



            if (Minecraft.getInstance().options.graphicsMode.getId() < 1) {
                float ang = 2 * MathHelper.sin(((period) / 35f) % (2 * (float) Math.PI));
                //renderCurvedSegment(builder, matrixStackIn, ang, 0, w, h, lu, lv, true, false,r,g,b);
            } else {

                boolean zAxis = tile.getDirection().getAxis().equals(Direction.Axis.Z);





                float t = (period);

                //float l = 15f; //wave length in pixels
                //float speed = 0.5f;
                //float invdamping = 0.3f;
                //float wavyness = 1f;




                int segmentlen = 1;
                /*
                if(Minecraft.getInstance().gameSettings.graphicFanciness.getId()==2){
                    invdamping =0.5f;
                    wavyness = 4;
                    speed=1f;


                }*/


                for (int z = 0; z < w; z += segmentlen) {

                    float ang = (float) ((wavyness + invdamping * z) * MathHelper.sin((float) ((((z / (l)) - speed * t) * (float) Math.PI) % (2 * (float) Math.PI))));


                    for(int p = 0; p<list.size(); p++) {
                        matrixStackIn.pushPose();

                        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entitySolid(FlagBlockTile.getFlagLocation(list.get(p).getFirst())));

                        int color = list.get(p).getSecond().getColorValue();
                        float b = (NativeImage.getR(color)) / 255f;
                        float g = (NativeImage.getG(color)) / 255f;
                        float r = (NativeImage.getB(color)) / 255f;


                        renderCurvedSegment(builder, matrixStackIn, ang, z, segmentlen, h, lu, lv, z + segmentlen >= w, zAxis, r, g, b);
                        //IVertexBuilder builder2 = bufferIn.getBuffer(RenderType.getEntityNoOutline(new ResourceLocation("supplementaries:textures/entity/flagcross.png")));

                        //renderCurvedSegment(builder2, matrixStackIn, ang, z, segmentlen, h, lu, lv, z + segmentlen >= w, zAxis);

                        matrixStackIn.popPose();
                    }
                    matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(ang));
                    matrixStackIn.translate(0, 0, segmentlen / 16f);
                    matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-ang));
                }


            }

            matrixStackIn.popPose();
        }

    }





    private static void renderCurvedSegment(IVertexBuilder builder, MatrixStack matrixStack, float angle, int posz, int lenght, int height, int lu, int lv, boolean end, boolean zAxis, float r, float g, float b) {
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

        int lus = (int)(lu*(zAxis? 0.8f : 1));
        int lvs = (int)(lv*(zAxis? 0.8f : 1));

        int nx = zAxis? 0 : 1;
        int nz = zAxis? 1 : 0;
        //0.4, 0.6

        //left
        matrixStack.pushPose();

        matrixStack.translate(hw,0,0);

        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, u, maxv, r, g, b, 1, lus, lvs, nx,0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lus, lvs, nx,0, nz);

        matrixStack.mulPose(rotation);
        matrixStack.translate(0, 0, l);

        RendererUtil.addVert(builder, matrixStack, 0, h, 0,maxu , v, r, g, b, 1, lus, lvs, nx, 0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxu, maxv, r, g, b, 1, lus, lvs, nx, 0, nz);

        matrixStack.popPose();

        //right
        matrixStack.pushPose();

        matrixStack.translate(-hw,0,0);

        RendererUtil.addVert(builder, matrixStack, 0, h, 0, u, v, r, g, b, 1, lus, lvs, -nx, 0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, u, maxv, r, g, b, 1, lus, lvs, -nx, 0, nz);

        matrixStack.mulPose(rotation);
        matrixStack.translate(0, 0, l);

        RendererUtil.addVert(builder, matrixStack, 0, 0, 0, maxu, maxv, r, g, b, 1, lus, lvs, -nx, 0, nz);
        RendererUtil.addVert(builder, matrixStack, 0, h, 0, maxu, v, r, g, b, 1, lus, lvs, -nx, 0, nz);

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