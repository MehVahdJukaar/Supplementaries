package net.mehvahdjukaar.supplementaries.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.ClockBlock;
import net.mehvahdjukaar.supplementaries.blocks.ClockBlockTile;
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


@OnlyIn(Dist.CLIENT)
public class ClockBlockTileRenderer extends TileEntityRenderer<ClockBlockTile> {
    public ClockBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(ClockBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.push();
        matrixStackIn.translate(0.5d, 0.5d, 0.5d);
        matrixStackIn.rotate(tile.getDirection().getRotation());
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90.0F));
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.interpolateAngle(partialTicks, tile.prevRoll, tile.roll)));

        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180));
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        matrixStackIn.translate(-0.5, -0.5, -0.5);
        BlockState state = Registry.CLOCK_BLOCK.get().getDefaultState().with(ClockBlock.TILE, true);
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.pop();
    }
}