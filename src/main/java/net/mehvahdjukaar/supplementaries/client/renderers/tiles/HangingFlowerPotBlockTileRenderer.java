package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.blocks.HangingFlowerPotBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.HangingFlowerPotBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.FlowerPotHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


public class HangingFlowerPotBlockTileRenderer extends TileEntityRenderer<HangingFlowerPotBlockTile> {
    protected final BlockRendererDispatcher blockRenderer;
    public HangingFlowerPotBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(HangingFlowerPotBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        BlockState state = CommonUtil.FESTIVITY.isAprilsFool()? FlowerPotHandler.getAprilPot() : tile.pot;
        BlockState state2 = tile.getBlockState().setValue(HangingFlowerPotBlock.TILE,true);

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);

        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle*1.5f, tile.angle*1.5f)));
        matrixStackIn.translate(-0.5, -0.5, -0.5);

        // render block
        //blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        //blockRenderer.renderBlock(state2, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        RendererUtil.renderBlockModel(state, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos());
        RendererUtil.renderBlockModel(state2, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos(), RenderType.cutout());

        matrixStackIn.popPose();




    }
}