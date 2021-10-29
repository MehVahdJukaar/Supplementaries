package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.block.tiles.DoormatBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.LOD;
import net.mehvahdjukaar.supplementaries.client.renderers.TextUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DoormatBlockTileRenderer implements BlockEntityRenderer<DoormatBlockTile> {
    private static final int LINE_MAX_WIDTH = 75;
    private final Font font;
    private final Camera camera;

    public DoormatBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
        camera = Minecraft.getInstance().gameRenderer.getMainCamera();
    }

    @Override
    public void render(DoormatBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        LOD lod = new LOD(camera, tile.getBlockPos());
        if (!lod.isNear()) return;

        poseStack.pushPose();

        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Const.rot(tile.getDirection().getOpposite()));

        // render text
        poseStack.translate(0, -0.010416667F * 20, -0.0625 - 0.005);
        poseStack.scale(0.010416667F, 0.010416667F, -0.010416667F);

        TextUtil.renderAllLines(tile.getTextHolder(), 15, font, LINE_MAX_WIDTH, poseStack, bufferIn, combinedLightIn, lod::isVeryNear);

        poseStack.popPose();
    }
}