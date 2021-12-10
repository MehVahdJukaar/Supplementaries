package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.JarBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBoatTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.Mth;


public class JarBoatTileRenderer implements BlockEntityRenderer<JarBoatTile> {

    public static final ModelResourceLocation BOAT_MODEL = new ModelResourceLocation(Supplementaries.MOD_ID+":jar_boat_ship", "");

    private final BlockRenderDispatcher blockRenderer;

    public JarBoatTileRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();

    }

    @Override
    public void render(JarBoatTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.pushPose();

        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(Const.rot((int) -tile.getBlockState().getValue(JarBlock.FACING).getOpposite().toYRot()));

        matrixStackIn.translate(0, -3/16f, 0);
        float t = ((System.currentTimeMillis() % 360000) / 1000f);
        matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(t)*1.7f));

        matrixStackIn.translate(-0.5, 0, -0.5);



        RendererUtil.renderBlockModel(BOAT_MODEL, matrixStackIn, bufferIn, blockRenderer, combinedLightIn, combinedOverlayIn, false);
        matrixStackIn.popPose();

    }
}