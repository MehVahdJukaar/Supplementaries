package net.mehvahdjukaar.supplementaries.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.OilLanternBlock;
import net.mehvahdjukaar.supplementaries.blocks.tiles.OilLanternBlockTile;
import net.mehvahdjukaar.supplementaries.renderers.Const;
import net.mehvahdjukaar.supplementaries.renderers.RendererUtil;
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


public class OilLanternBlockTileRenderer extends TileEntityRenderer<OilLanternBlockTile> {
    public OilLanternBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(OilLanternBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        AttachFace face = tile.getBlockState().get(OilLanternBlock.FACE);
        if(face==AttachFace.FLOOR)return;

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockState state = tile.getBlockState().with(OilLanternBlock.FACE, AttachFace.FLOOR).with(OilLanternBlock.FACING, Direction.NORTH);

        matrixStackIn.push();
        // rotate towards direction
        matrixStackIn.translate(0.5, 0.875, 0.5);
        matrixStackIn.rotate(tile.getDirection().getOpposite().getRotation());
        matrixStackIn.rotate(Const.XN90);

        // animation
        if(face==AttachFace.WALL){
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle, tile.angle)));
            matrixStackIn.translate(-0.5, -0.75, -0.375);
        }
        else {
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, tile.prevAngle*1.5f, tile.angle*1.5f)));
            matrixStackIn.translate(-0.5, -0.5625, -0.5);
        }
        // render block
        RendererUtil.renderBlockPlus(state, matrixStackIn, bufferIn,
                blockRenderer, tile.getWorld(), tile.getPos(),  RenderType.getCutout());
        //blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.pop();




    }
}