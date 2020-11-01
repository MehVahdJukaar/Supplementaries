package net.mehvahdjukaar.supplementaries.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.blocks.PistonLauncherArmBlockTile;
import net.mehvahdjukaar.supplementaries.blocks.PistonLauncherHeadBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class PistonLauncherArmBlockTileRenderer extends TileEntityRenderer<PistonLauncherArmBlockTile> {
    public PistonLauncherArmBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(PistonLauncherArmBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.push();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.rotate(tile.getDirection().getOpposite().getRotation());
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180.0F));
        matrixStackIn.translate(-0.5, -0.5, -0.5);
        matrixStackIn.translate(0, MathHelper.lerp(partialTicks, tile.getPrevOffset(), tile.getOffset()), 0);
        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        boolean flag1 = tile.getExtending() == tile.getAge() < 2;
        BlockState state = Registry.PISTON_LAUNCHER_HEAD.get().getDefaultState().with(PistonLauncherHeadBlock.FACING, Direction.UP).with(BlockStateProperties.SHORT,
                flag1);
        blockRenderer.renderBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.pop();
    }
}