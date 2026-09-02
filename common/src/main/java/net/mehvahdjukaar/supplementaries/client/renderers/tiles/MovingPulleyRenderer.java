package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingPulleyBlockEntity;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.PistonHeadRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Basically based off vanilla piston head renderer
public class MovingPulleyRenderer extends PistonHeadRenderer {

    private final BlockRenderDispatcher blockRenderer;

    public MovingPulleyRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(PistonMovingBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean isLastBlock = be instanceof MovingPulleyBlockEntity movingPulleyBE && movingPulleyBE.getLeadingState() != null;
        MovingPulleyBlockEntity movPulleyBE = isLastBlock ? (MovingPulleyBlockEntity) be : null;
        super.render(be, partialTick, pose, buffer, packedLight, packedOverlay);

        if (movPulleyBE != null) {
            BlockState leading = movPulleyBE.getLeadingState();
            if (leading != null && !leading.isAir()) {
                Level level = movPulleyBE.getLevel();
                if (level != null) {

                    Direction dir = be.getDirection();
                    float progress = be.getProgress(partialTick);
                    float adjustedProg = movPulleyBE.isExtendPhantom() ? (progress - 2.0F) : progress;
                    pose.pushPose();
                    pose.translate(adjustedProg * dir.getStepX(), adjustedProg * dir.getStepY(), adjustedProg * dir.getStepZ());
                    BlockPos pos = be.getBlockPos();
                    RenderType type = ItemBlockRenderTypes.getMovingBlockRenderType(leading);
                    VertexConsumer vc = buffer.getBuffer(type);
                    blockRenderer.getModelRenderer().tesselateBlock(
                            level, blockRenderer.getBlockModel(leading), leading, pos, pose, vc,
                            false, RandomSource.create(),
                            leading.getSeed(pos), packedOverlay);
                    pose.popPose();
                }
            }
        }
    }
}
