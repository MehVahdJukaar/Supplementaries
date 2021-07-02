package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.block.tiles.CrackedBellBlockTile;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

public class CrackedBellTileEntityRenderer extends TileEntityRenderer<CrackedBellBlockTile> {
    private final BlockRendererDispatcher blockRenderer;
    private final BlockState floorBell;


    public CrackedBellTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
        floorBell = Registry.CRACKED_BELL.get().defaultBlockState();
    }

    public void render(CrackedBellBlockTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {

        if(tile.isOnFloor())return;

        matrixStack.pushPose();

        if (tile.shaking) {
            matrixStack.translate(0.5, 1-0.1875, 0.5);
            float f = (float)tile.ticks + partialTicks;

            float f3 = MathHelper.sin(f / (float)Math.PI) / (4.0F + f / 3.0F);
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
