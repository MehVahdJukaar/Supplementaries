package net.mehvahdjukaar.supplementaries.renderers;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.blocks.FlagBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FlagBlockTileRenderer extends TileEntityRenderer<FlagBlockTile> {
    public FlagBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(FlagBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntitySolid(new ResourceLocation("supplementaries:textures/entity/flag.png")));

        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';

        int w = 24;
        int h = 16;

        matrixStackIn.push();
        matrixStackIn.translate(0.5,0,0.5);
        matrixStackIn.rotate(tile.getDirection().getRotation());
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90)) ;
        matrixStackIn.translate(0,0,1/16f);


        if(Minecraft.getInstance().gameSettings.graphicFanciness.func_238162_a_()<1){
            float ang =2*MathHelper.sin(((tile.counter+partialTicks) / 35f)% (2 * (float) Math.PI));
            renderCurvedSegment(builder, matrixStackIn, ang, 0, w, h, lu, lv, true, false);
        }
        else {

            boolean zAxis = tile.getDirection().getAxis().equals(Direction.Axis.Z);


            float t = (tile.counter+partialTicks) / (20f);

            float l = 15f; //wave length in pixels

            float speed = 0.5f;
            float invdamping = 0.3f;
            float wavyness = 1f;

            int segmentlen = 1;

            if(Minecraft.getInstance().gameSettings.graphicFanciness.func_238162_a_()==2){
                invdamping =0.5f;
                wavyness = 4;
                speed=1f;


            }


            for (int z = 0; z < w; z+=segmentlen) {

                float ang = (wavyness + invdamping*z) * MathHelper.sin((((z / (l)) - speed * t) * (float) Math.PI) % (2 * (float) Math.PI));



                renderCurvedSegment(builder, matrixStackIn, ang, z, segmentlen, h, lu, lv, z + segmentlen >= w, zAxis);
//IVertexBuilder builder2 = bufferIn.getBuffer(RenderType.getEntityNoOutline(new ResourceLocation("supplementaries:textures/entity/flagcross.png")));

                //renderCurvedSegment(builder2, matrixStackIn, ang, z, segmentlen, h, lu, lv, z + segmentlen >= w, zAxis);


                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(ang));
                matrixStackIn.translate(0, 0, segmentlen/16f);
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-ang));
            }



        }

        matrixStackIn.pop();

    }




    @OnlyIn(Dist.CLIENT)
    private static void renderCurvedSegment(IVertexBuilder builder, MatrixStack matrixStackIn, float angle, int posz, int lenght, int height, int lu, int lv, boolean end, boolean zAxis) {
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
        matrixStackIn.push();

        matrixStackIn.translate(hw,0,0);

        CommonUtil.addVert(builder, matrixStackIn, 0, 0, 0, u, maxv, 1, 1, 1, 1, lus, lvs, nx,0, nz);
        CommonUtil.addVert(builder, matrixStackIn, 0, h, 0, u, v, 1, 1, 1, 1, lus, lvs, nx,0, nz);

        matrixStackIn.rotate(rotation);
        matrixStackIn.translate(0, 0, l);

        CommonUtil.addVert(builder, matrixStackIn, 0, h, 0,maxu , v, 1, 1, 1, 1, lus, lvs, nx, 0, nz);
        CommonUtil.addVert(builder, matrixStackIn, 0, 0, 0, maxu, maxv, 1, 1, 1, 1, lus, lvs, nx, 0, nz);

        matrixStackIn.pop();

        //right
        matrixStackIn.push();

        matrixStackIn.translate(-hw,0,0);

        CommonUtil.addVert(builder, matrixStackIn, 0, h, 0, u, v, 1, 1, 1, 1, lus, lvs, -nx, 0, nz);
        CommonUtil.addVert(builder, matrixStackIn, 0, 0, 0, u, maxv, 1, 1, 1, 1, lus, lvs, -nx, 0, nz);

        matrixStackIn.rotate(rotation);
        matrixStackIn.translate(0, 0, l);

        CommonUtil.addVert(builder, matrixStackIn, 0, 0, 0, maxu, maxv, 1, 1, 1, 1, lus, lvs, -nx, 0, nz);
        CommonUtil.addVert(builder, matrixStackIn, 0, h, 0, maxu, v, 1, 1, 1, 1, lus, lvs, -nx, 0, nz);

        matrixStackIn.pop();

        //top
        matrixStackIn.push();

        matrixStackIn.translate(hw,0,0);

        CommonUtil.addVert(builder, matrixStackIn, 0, h, 0, u, v, 1, 1, 1, 1, lu, lv, 0, 1, 0);
        matrixStackIn.translate(-w,0,0);
        CommonUtil.addVert(builder, matrixStackIn, 0, h, 0, u, w, 1, 1, 1, 1, lu, lv, 0, 1, 0);

        matrixStackIn.rotate(rotation);
        matrixStackIn.translate(0, 0, l);

        CommonUtil.addVert(builder, matrixStackIn, 0, h, 0, maxu, w, 1, 1, 1, 1, lu, lv, 0, 1, 0);
        matrixStackIn.rotate(rotation2);
        matrixStackIn.translate(w,0,0);
        CommonUtil.addVert(builder, matrixStackIn, 0, h, 0, maxu, v, 1, 1, 1, 1, lu, lv, 0, 1, 0);

        matrixStackIn.pop();

        //bottom
        matrixStackIn.push();

        matrixStackIn.translate(-hw,0,0);

        CommonUtil.addVert(builder, matrixStackIn, 0, 0, 0, u, h-w, 1, 1, 1, 1, lu, lv, -1, 0, 0);
        matrixStackIn.translate(w,0,0);
        CommonUtil.addVert(builder, matrixStackIn, 0, 0, 0, u, h, 1, 1, 1, 1, lu, lv, 0, -1, 0);

        matrixStackIn.rotate(rotation);
        matrixStackIn.translate(0, 0, l);

        CommonUtil.addVert(builder, matrixStackIn, 0, 0, 0, maxu, h, 1, 1, 1, 1, lu, lv, 0, -1, 0);
        matrixStackIn.rotate(rotation2);
        matrixStackIn.translate(-w,0,0);
        CommonUtil.addVert(builder, matrixStackIn, 0, 0, 0, maxu, h-w, 1, 1, 1, 1, lu, lv, 0, -1, 0);


        matrixStackIn.pop();

        //end
        if(end) {
            matrixStackIn.push();

            matrixStackIn.rotate(rotation);
            matrixStackIn.translate(0, 0, l);
            matrixStackIn.rotate(rotation2);
            matrixStackIn.translate(-hw, 0, 0);

            CommonUtil.addVert(builder, matrixStackIn, 0, h, 0, maxu-(1/textw), 0, 1, 1, 1, 1, lu, lv, 0, 0, 1);
            CommonUtil.addVert(builder, matrixStackIn, 0, 0, 0, maxu-(1/textw), maxv, 1, 1, 1, 1, lu, lv, 0, 0, 1);

            matrixStackIn.translate(w, 0, 0);

            CommonUtil.addVert(builder, matrixStackIn, 0, 0, 0, maxu, maxv, 1, 1, 1, 1, lu, lv, 0, 0, 1);
            CommonUtil.addVert(builder, matrixStackIn, 0, h, 0, maxu, 0, 1, 1, 1, 1, lu, lv, 0, 0, 1);

            matrixStackIn.pop();
        }
    }


}