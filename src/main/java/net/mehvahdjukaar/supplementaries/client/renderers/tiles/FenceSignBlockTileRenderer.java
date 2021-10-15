package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.block.tiles.FenceSignBlockTile;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.model.Material;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.util.FormattedCharSequence;
import com.mojang.math.Vector3f;

import java.util.List;


public class FenceSignBlockTileRenderer extends BlockEntityRenderer<FenceSignBlockTile> {
    private final BlockRenderDispatcher blockRenderer;
    public final ModelPart signBoard = new ModelPart(64, 32, 0, 0);

    public FenceSignBlockTileRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
        signBoard.setPos(0.0F, -4.0F, 0.0F);
        signBoard.addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F, 0.0F);
    }

    @Override
    public void render(FenceSignBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        BlockState fence = tile.fenceBlock;
        if(fence !=null){
            RendererUtil.renderBlockModel(fence, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos());
            //blockRenderer.renderBlock(fence, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        }

        BlockState sign = tile.signBlock;
        //render signs
        if(fence !=null){


            // sign code
            Font fontrenderer = this.renderer.getFont();
            int i = tile.textHolder.textColor.getTextColor();
            int j = (int) ((double) NativeImage.getR(i) * 0.4D);
            int k = (int) ((double) NativeImage.getG(i) * 0.4D);
            int l = (int) ((double) NativeImage.getB(i) * 0.4D);
            int i1 = NativeImage.combine(0, l, k, j);



            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);



            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(tile.signFacing.toYRot()));
            //matrixStackIn.rotate(Const.YN90);

            //sign block
            matrixStackIn.pushPose();


            matrixStackIn.scale(1,-1,-1);
            Material material = Materials.BELLOWS_MATERIAL;
            VertexConsumer builder =  material.buffer(bufferIn, RenderType::entitySolid);
            signBoard.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

            matrixStackIn.popPose();

            //text up
            matrixStackIn.translate(-0.03125, 0.28125, 0.1875 + 0.005);
            matrixStackIn.scale(0.010416667F, -0.010416667F, 0.010416667F);
            matrixStackIn.translate(0, 1, 0);


                FormattedCharSequence ireorderingprocessor = tile.textHolder.getRenderText(0, (p_243502_1_) -> {
                    List<FormattedCharSequence> list = fontrenderer.split(p_243502_1_, 90);
                    return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
                });
                if (ireorderingprocessor != null) {
                    float f3 = (float)(-fontrenderer.width(ireorderingprocessor) / 2);
                    fontrenderer.drawInBatch(ireorderingprocessor, f3, (float)(-5), i1, false, matrixStackIn.last().pose(), bufferIn, false, 0, combinedLightIn);
                }




            matrixStackIn.popPose();
        }

    }
}
