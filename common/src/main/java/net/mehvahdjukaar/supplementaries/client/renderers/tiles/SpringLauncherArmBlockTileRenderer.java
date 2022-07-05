package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.client.renderUtils.RotHlpr;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SpringLauncherHeadBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SpringLauncherArmBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.data.EmptyModelData;


public class SpringLauncherArmBlockTileRenderer implements BlockEntityRenderer<SpringLauncherArmBlockTile> {
    private final BlockRenderDispatcher blockRenderer;

    public SpringLauncherArmBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(SpringLauncherArmBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(RotHlpr.rot(tile.getDirection().getOpposite()));
        matrixStackIn.mulPose(RotHlpr.X180);
        matrixStackIn.translate(-0.5, -0.5, -0.5);
        matrixStackIn.translate(0, Mth.lerp(partialTicks, tile.prevOffset, tile.offset), 0);
        boolean flag1 = tile.getExtending() == tile.age < 2;
        BlockState state = ModRegistry.SPRING_LAUNCHER_HEAD.get().defaultBlockState().setValue(SpringLauncherHeadBlock.FACING, Direction.UP).setValue(BlockStateProperties.SHORT, flag1);
        //RendererUtil.renderBlockPlus(state, matrixStackIn, bufferIn, blockRenderer, tile.getWorld(), tile.getPos());
        blockRenderer.renderSingleBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.popPose();

    }
}