package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.tiles.EnhancedLanternBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


public class EnhancedLanternBlockTileRenderer<T extends  EnhancedLanternBlockTile> extends TileEntityRenderer<T> {
    public EnhancedLanternBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }


    public void renderLantern(T tile, BlockState state, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
                              int combinedLightIn, int combinedOverlayIn, boolean ceiling){
        matrixStackIn.pushPose();
        // rotate towards direction
        matrixStackIn.translate(0.5, 0.875, 0.5);
        matrixStackIn.mulPose(tile.getDirection().getOpposite().getRotation());
        matrixStackIn.mulPose(Const.XN90);
        // animation
        if(ceiling) {
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle * 1.5f, tile.angle * 1.5f)));
            matrixStackIn.translate(-0.5, -0.5625, -0.5);
        }
        else {
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle, tile.angle)));
            matrixStackIn.translate(-0.5, -0.75, -0.375);
        }
        // render block
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        RendererUtil.renderBlockPlus(state, matrixStackIn, bufferIn, blockRenderer, tile.getLevel(), tile.getBlockPos());
        //blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.popPose();
    }


    @Override
    public void render(T tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        BlockState state = tile.getBlockState().getBlock().defaultBlockState();

        this.renderLantern(tile,state,partialTicks,matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn,false);
    }
}