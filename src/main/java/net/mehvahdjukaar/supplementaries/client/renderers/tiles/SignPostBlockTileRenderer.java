package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;


public class SignPostBlockTileRenderer extends TileEntityRenderer<SignPostBlockTile> {
    public static final ModelRenderer signModel = new ModelRenderer(64, 16, 0, 0);
   //TODO: make other tiles this way
    static {
        signModel.setRotationPoint(0.0F, -4.0F, 0.0F);
        signModel.setTextureOffset(0, 10).addBox(-12.0F, -1.0F, -3.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);
        signModel.setTextureOffset(0, 0).addBox(-8.0F, -3.0F, -3.0F, 16.0F, 5.0F, 1.0F, 0.0F, false);
        signModel.setTextureOffset(0, 6).addBox(-10.0F, -2.0F, -3.0F, 2.0F, 3.0F, 1.0F, 0.0F, false);
    }

    public SignPostBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
   }


    @Override
    public void render(SignPostBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        BlockState fence = tile.fenceBlock;
        if(fence !=null){
            BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
            RendererUtil.renderBlockPlus(fence, matrixStackIn, bufferIn, blockRenderer, tile.getWorld(), tile.getPos());
            //blockRenderer.renderBlock(fence, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        }

        boolean up = tile.up;
        boolean down = tile.down;
        //render signs
        if(up||down){


            // sign code
            FontRenderer fontrenderer = this.renderDispatcher.getFontRenderer();
            int i = tile.textHolder.textColor.getTextColor();
            int j = (int) ((double) NativeImage.getRed(i) * 0.4D);
            int k = (int) ((double) NativeImage.getGreen(i) * 0.4D);
            int l = (int) ((double) NativeImage.getBlue(i) * 0.4D);
            int i1 = NativeImage.getCombined(0, l, k, j);



            matrixStackIn.push();
            matrixStackIn.translate(0.5, 0.5, 0.5);




            if(up){
                matrixStackIn.push();

                boolean left = tile.leftUp;
                int o = left ? 1 : -1;

                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(tile.yawUp-90));
                //matrixStackIn.rotate(Const.YN90);

                //sign block
                matrixStackIn.push();

                if(!left){
                    matrixStackIn.rotate(Const.YN180);
                    matrixStackIn.translate(0, 0, -0.3125);
                }

                matrixStackIn.scale(1,-1,-1);
                RenderMaterial material = Materials.SIGN_POSTS_MATERIAL.get(tile.woodTypeUp);
                IVertexBuilder builder =  material.getBuffer(bufferIn, RenderType::getEntitySolid);
                signModel.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

                matrixStackIn.pop();

                //text up
                matrixStackIn.translate(-0.03125*o, 0.28125, 0.1875 + 0.005);
                matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
                matrixStackIn.translate(0, 1, 0);


                    IReorderingProcessor ireorderingprocessor = tile.textHolder.getRenderText(0, (p_243502_1_) -> {
                        List<IReorderingProcessor> list = fontrenderer.trimStringToWidth(p_243502_1_, 90);
                        return list.isEmpty() ? IReorderingProcessor.field_242232_a : list.get(0);
                    });
                    if (ireorderingprocessor != null) {
                        float f3 = (float)(-fontrenderer.func_243245_a(ireorderingprocessor) / 2);
                        fontrenderer.func_238416_a_(ireorderingprocessor, f3, (float)(-5), i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);
                    }

                matrixStackIn.pop();
            }
            if(down){
                matrixStackIn.push();

                boolean left = tile.leftDown;
                int o = left ? 1 : -1;

                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(tile.yawDown-90));
                matrixStackIn.translate(0, -0.5, 0);

                //sign block
                matrixStackIn.push();

                if(!left){
                    matrixStackIn.rotate(Const.YN180);
                    matrixStackIn.translate(0, 0, -0.3125);
                }

                matrixStackIn.scale(1,-1,-1);
                RenderMaterial material = Materials.SIGN_POSTS_MATERIAL.get(tile.woodTypeDown);
                IVertexBuilder builder =  material.getBuffer(bufferIn, RenderType::getEntitySolid);
                signModel.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

                matrixStackIn.pop();

                //text down
                matrixStackIn.translate(-0.03125*o, 0.28125, 0.1875 + 0.005);
                matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
                matrixStackIn.translate(0, 1, 0);

                IReorderingProcessor ireorderingprocessor = tile.textHolder.getRenderText(1, (p_243502_1_) -> {
                    List<IReorderingProcessor> list = fontrenderer.trimStringToWidth(p_243502_1_, 90);
                    return list.isEmpty() ? IReorderingProcessor.field_242232_a : list.get(0);
                });
                if (ireorderingprocessor != null) {
                    float f3 = (float)(-fontrenderer.func_243245_a(ireorderingprocessor) / 2);
                    fontrenderer.func_238416_a_(ireorderingprocessor, f3, (float)(-5), i1, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, 0, combinedLightIn);
                }


                matrixStackIn.pop();
            }
            matrixStackIn.pop();
        }

    }
}
