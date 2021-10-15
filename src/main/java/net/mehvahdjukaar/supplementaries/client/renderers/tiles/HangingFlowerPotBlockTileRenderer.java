package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.block.blocks.HangingFlowerPotBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.HangingFlowerPotBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.FlowerPotHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;


public class HangingFlowerPotBlockTileRenderer extends BlockEntityRenderer<HangingFlowerPotBlockTile> {

    protected final BlockRenderDispatcher blockRenderer;

    public HangingFlowerPotBlockTileRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public void render(HangingFlowerPotBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        BlockState state = CommonUtil.FESTIVITY.isAprilsFool() ? FlowerPotHandler.getAprilPot() : tile.getHeldBlock();
        BlockState state2 = tile.getBlockState().setValue(HangingFlowerPotBlock.TILE, true);

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);

        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, tile.prevAngle * 1.5f, tile.angle * 1.5f)));
        matrixStackIn.translate(-0.5, -0.5, -0.5);

        // render block
        //blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        //blockRenderer.renderBlock(state2, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        RendererUtil.renderBlockModel(state, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos());
        RendererUtil.renderBlockModel(state2, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos(), RenderType.cutout());

        matrixStackIn.popPose();


    }
}