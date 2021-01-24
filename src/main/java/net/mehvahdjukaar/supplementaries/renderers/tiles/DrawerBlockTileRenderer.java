package net.mehvahdjukaar.supplementaries.renderers.tiles;
/*
import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.DrawersBlock;
import net.mehvahdjukaar.supplementaries.blocks.DrawersBlockTile;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;


public class DrawerBlockTileRenderer extends TileEntityRenderer<DrawersBlockTile> {
    public DrawerBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(DrawersBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.push();
        matrixStackIn.translate(0.5, 0.5, 0.5);

        matrixStackIn.rotate(tile.getDirection().getRotation());
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90));
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-180));
        matrixStackIn.translate(0,0,-0.5*tile.opening);
        matrixStackIn.translate(-0.5, -0.5, -0.5);
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockState state = Registry.DRAWERS.get().getDefaultState().with(DrawersBlock.TILE, true);
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);

        BlockState state1 = Registry.DRAWERS.get().getDefaultState().with(DrawersBlock.TILE, false);
        blockRenderer.renderBlock(state1, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);

        matrixStackIn.translate(0, 0.5, 0);
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.pop();
        matrixStackIn.push();
        matrixStackIn.translate(0,1,0);
        blockRenderer.renderBlock(state1, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.pop();

    }
}
*/
