package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.LOD;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;


public class SignPostBlockTileRenderer extends TileEntityRenderer<SignPostBlockTile> {

    public static final ModelRenderer signModel = new ModelRenderer(64, 16, 0, 0);
   //TODO: make other tiles this way
    static {
        signModel.setPos(0.0F, 0.0F, 0.0F);
        signModel.texOffs(0, 10).addBox(-12.0F, -5.0F, -3.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        signModel.texOffs(0, 0).addBox(-8.0F, -7.0F, -3.0F, 16.0F, 5.0F, 1.0F, 0.0F, false);
        signModel.texOffs(0, 6).addBox(-10.0F, -6.0F, -3.0F, 2.0F, 3.0F, 1.0F, 0.0F, false);
    }

    public SignPostBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);

   }

    @Override
    public void render(SignPostBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        BlockPos pos = tile.getBlockPos();
        Vector3d cameraPos = this.renderer.camera.getPosition();

        //don't render signs from far away

        LOD lod = new LOD(cameraPos,pos);

        boolean up = tile.up;
        boolean down = tile.down;
        //render signs
        if(up||down){

            float relAngle = LOD.getRelativeAngle(cameraPos, pos);

            // sign code
            FontRenderer fontrenderer = this.renderer.getFont();
            int i = tile.textHolder.textColor.getTextColor();
            int j = (int) ((double) NativeImage.getR(i) * 0.4D);
            int k = (int) ((double) NativeImage.getG(i) * 0.4D);
            int l = (int) ((double) NativeImage.getB(i) * 0.4D);
            int i1 = NativeImage.combine(0, l, k, j);


            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);

            if(up){
                matrixStackIn.pushPose();

                boolean left = tile.leftUp;
                int o = left ? 1 : -1;

                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(tile.yawUp-90));
                //matrixStackIn.rotate(Const.YN90);

                //sign block
                matrixStackIn.pushPose();

                if(!left){
                    matrixStackIn.mulPose(Const.YN180);
                    matrixStackIn.translate(0, 0, -0.3125);
                }

                matrixStackIn.scale(1,-1,-1);
                RenderMaterial material = Materials.SIGN_POSTS_MATERIALS.get(tile.woodTypeUp);
                IVertexBuilder builder =  material.buffer(bufferIn, RenderType::entitySolid);
                signModel.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

                matrixStackIn.popPose();

                //culling
                if(lod.isNear() && LOD.isOutOfFocus(relAngle,tile.yawUp+90,2)) {

                    //text up
                    matrixStackIn.translate(-0.03125 * o, 0.28125, 0.1875 + 0.005);
                    matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
                    matrixStackIn.translate(0, 1, 0);


                    IReorderingProcessor ireorderingprocessor = tile.textHolder.getRenderText(0, (t) -> {
                        List<IReorderingProcessor> list = fontrenderer.split(t, 90);
                        return list.isEmpty() ? IReorderingProcessor.EMPTY : list.get(0);
                    });
                    if (ireorderingprocessor != null) {
                        float f3 = (float) (-fontrenderer.width(ireorderingprocessor) / 2);
                        fontrenderer.drawInBatch(ireorderingprocessor, f3, (float) (-5), i1, false, matrixStackIn.last().pose(), bufferIn, false, 0, combinedLightIn);
                    }
                }

                matrixStackIn.popPose();
            }

            if(down){
                matrixStackIn.pushPose();

                boolean left = tile.leftDown;
                int o = left ? 1 : -1;

                matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(tile.yawDown-90));
                matrixStackIn.translate(0, -0.5, 0);

                //sign block
                matrixStackIn.pushPose();

                if(!left){
                    matrixStackIn.mulPose(Const.YN180);
                    matrixStackIn.translate(0, 0, -0.3125);
                }

                matrixStackIn.scale(1,-1,-1);
                RenderMaterial material = Materials.SIGN_POSTS_MATERIALS.get(tile.woodTypeDown);
                IVertexBuilder builder =  material.buffer(bufferIn, RenderType::entitySolid);
                signModel.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

                matrixStackIn.popPose();

                if(lod.isNear() && LOD.isOutOfFocus(relAngle,tile.yawDown+90,2)) {

                    //text down
                    matrixStackIn.translate(-0.03125 * o, 0.28125, 0.1875 + 0.005);
                    matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
                    matrixStackIn.translate(0, 1, 0);

                    IReorderingProcessor ireorderingprocessor = tile.textHolder.getRenderText(1, (p_243502_1_) -> {
                        List<IReorderingProcessor> list = fontrenderer.split(p_243502_1_, 90);
                        return list.isEmpty() ? IReorderingProcessor.EMPTY : list.get(0);
                    });
                    if (ireorderingprocessor != null) {
                        float f3 = (float) (-fontrenderer.width(ireorderingprocessor) / 2);
                        fontrenderer.drawInBatch(ireorderingprocessor, f3, (float) (-5), i1, false, matrixStackIn.last().pose(), bufferIn, false, 0, combinedLightIn);
                    }
                }

                matrixStackIn.popPose();
            }
            matrixStackIn.popPose();
        }

    }
}
