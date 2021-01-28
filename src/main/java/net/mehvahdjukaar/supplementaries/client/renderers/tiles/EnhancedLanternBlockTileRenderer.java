package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.blocks.EnhancedLanternBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.OilLanternBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.EnhancedLanternBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


public class EnhancedLanternBlockTileRenderer<T extends  EnhancedLanternBlockTile> extends TileEntityRenderer<T> {
    public EnhancedLanternBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }


    public void renderLantern(T tile, BlockState state, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
                              int combinedLightIn, int combinedOverlayIn, boolean ceiling){
        matrixStackIn.push();
        // rotate towards direction
        matrixStackIn.translate(0.5, 0.875, 0.5);
        matrixStackIn.rotate(tile.getDirection().getOpposite().getRotation());
        matrixStackIn.rotate(Const.XN90);
        // animation
        if(ceiling) {
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle * 1.5f, tile.angle * 1.5f)));
            matrixStackIn.translate(-0.5, -0.5625, -0.5);
        }
        else {
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle, tile.angle)));
            matrixStackIn.translate(-0.5, -0.75, -0.375);
        }
        // render block
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        RendererUtil.renderBlockPlus(state, matrixStackIn, bufferIn, blockRenderer, tile.getWorld(), tile.getPos());
        //blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.pop();
    }


    @Override
    public void render(T tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        BlockState state = tile.getBlockState().getBlock().getDefaultState();
        this.renderLantern(tile,state,partialTicks,matrixStackIn,bufferIn,combinedLightIn,combinedOverlayIn,false);
    }
}