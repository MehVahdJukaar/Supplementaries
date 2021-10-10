package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBoatTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;


public class JarBoatTileRenderer extends TileEntityRenderer<JarBoatTile> {

    public static final ModelResourceLocation LOC = new ModelResourceLocation(Supplementaries.MOD_ID+":jar_boat_ship", "");

    private final BlockRendererDispatcher blockRenderer;

    public JarBoatTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();

    }

    @Override
    public void render(JarBoatTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.pushPose();

        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(Const.rot((int) -tile.getBlockState().getValue(JarBlock.FACING).getOpposite().toYRot()));

        matrixStackIn.translate(0, -3/16f, 0);
        float t = ((System.currentTimeMillis() % 360000) / 1000f);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(MathHelper.sin(t)*1.7f));

        matrixStackIn.translate(-0.5, 0, -0.5);



        RendererUtil.renderBlockModel(LOC, matrixStackIn, bufferIn, blockRenderer, combinedLightIn, combinedOverlayIn, false);
        matrixStackIn.popPose();

    }
}