package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.blocks.FlowerBoxBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.FlowerBoxBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class FlowerBoxBlockTileRenderer extends TileEntityRenderer<FlowerBoxBlockTile> {
    protected final BlockRendererDispatcher blockRenderer;
    protected static final List<Quaternion> rots = Arrays.asList(Const.Y90,Const.Y180,Const.YN90);
    public FlowerBoxBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }



    @Override
    public void render(FlowerBoxBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        if(!tile.isEmpty()){


            Random rand = new Random(tile.getBlockPos().asLong());

            matrixStackIn.pushPose();
            matrixStackIn.translate(0.5, 0.5, 0.5);

            float yaw = tile.getYaw();
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(yaw));
            matrixStackIn.translate(-0.3125,-3/16f,0);

            if(tile.getBlockState().getValue(FlowerBoxBlock.FLOOR))
                matrixStackIn.translate(0,0,-0.3125);

            matrixStackIn.scale(0.625f, 0.625f, 0.625f);

            //IBakedModel ibakedmodel = itemRenderer.getModel(stack, tile.getLevel(), null);
            //if(ibakedmodel.isGui3d()&&ClientConfigs.cached.SHELF_TRANSLATE)matrixStackIn.translate(0,-0.25,0);
            //ibakedmodel.getQuads(null,null,null).get(1).getSprite();
            //RendererUtil.renderBlockPlus(tile.flower1, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos());


            blockRenderer.renderBlock(tile.flower1, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
            if(tile.flower1_up!=null) {
                matrixStackIn.translate(0, 1, 0);
                blockRenderer.renderBlock(tile.flower1_up, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
                matrixStackIn.translate(0, -1, 0);
            }

            matrixStackIn.translate(0.5,0,0);

            blockRenderer.renderBlock(tile.flower2, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
            if(tile.flower2_up!=null) {
                matrixStackIn.translate(0, 1, 0);
                blockRenderer.renderBlock(tile.flower2_up, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
                matrixStackIn.translate(0, -1, 0);
            }

            matrixStackIn.translate(-1,0,0);


            blockRenderer.renderBlock(tile.flower0, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
            if(tile.flower0_up!=null) {
                matrixStackIn.translate(0, 1, 0);
                blockRenderer.renderBlock(tile.flower0_up, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
                matrixStackIn.translate(0, -1, 0);
            }




            matrixStackIn.popPose();
        }

    }

}