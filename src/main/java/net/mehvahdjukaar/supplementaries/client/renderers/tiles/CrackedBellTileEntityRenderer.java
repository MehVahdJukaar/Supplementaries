package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.block.tiles.CrackedBellBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

public class CrackedBellTileEntityRenderer extends BlockEntityRenderer<CrackedBellBlockTile> {
    private final BlockRenderDispatcher blockRenderer;
    private final BlockState floorBell;


    public CrackedBellTileEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
        floorBell = ModRegistry.CRACKED_BELL.get().defaultBlockState();
    }

    public void render(CrackedBellBlockTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        if(tile.isOnFloor())return;

        matrixStack.pushPose();

        if (tile.shaking) {
            matrixStack.translate(0.5, 1-0.1875, 0.5);
            float f = (float)tile.ticks + partialTicks;

            float f3 = Mth.sin(f / (float)Math.PI) / (4.0F + f / 3.0F);
            if (tile.clickDirection == Direction.NORTH) {
                matrixStack.mulPose(Vector3f.XP.rotation(-f3));
            } else if (tile.clickDirection == Direction.SOUTH) {
                matrixStack.mulPose(Vector3f.XP.rotation(f3));
            } else if (tile.clickDirection == Direction.EAST) {
                matrixStack.mulPose(Vector3f.ZP.rotation(-f3));
            } else if (tile.clickDirection == Direction.WEST) {
                matrixStack.mulPose(Vector3f.ZP.rotation(f3));
            }

            matrixStack.translate(-0.5, -1+0.1875, -0.5);
        }
        matrixStack.translate(0, 0.0625, 0);
        blockRenderer.renderBlock(floorBell, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStack.popPose();
    }
}
