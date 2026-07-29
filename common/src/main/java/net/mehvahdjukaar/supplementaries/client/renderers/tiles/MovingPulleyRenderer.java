package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.client.renderers.DepthMaskUtils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingPulleyBlockEntity;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs.PulleyMaskMode;
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
import org.joml.Matrix4f;

/**
 * Vanilla piston rendering + a "leading phantom" block one slot ahead in the push direction.
 * The phantom shares the carried block's progress curve, so a rope being consumed by a pulley
 * animates smoothly from its source slot into the pulley body alongside the rest of the chain
 * sliding up. The phantom is purely visual, see {@link MovingPulleyBlockEntity#getLeadingState}.
 * <p>
 * For the topmost moving block (the one carrying the leading phantom) we additionally write a
 * 1-block mask cube at the pulley's world position around the carried block + phantom draws,
 * so they appear to disappear into / emerge from the pulley body. Mask strategy is chosen by
 * {@link ClientConfigs.Blocks#PULLEY_MASK_MODE}.
 */
public class MovingPulleyRenderer extends PistonHeadRenderer {

    private final BlockRenderDispatcher blockRenderer;

    public MovingPulleyRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(PistonMovingBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        boolean isLastBlock = be instanceof MovingPulleyBlockEntity mpbe && mpbe.getLeadingState() != null;
        MovingPulleyBlockEntity mpbe = isLastBlock ? (MovingPulleyBlockEntity) be : null;
        PulleyMaskMode mode = isLastBlock ? ClientConfigs.Blocks.PULLEY_MASK_MODE.get() : PulleyMaskMode.OFF;
        boolean masking = mode != PulleyMaskMode.OFF;

        // Pose matrix of the pulley-position cube (centred on the pulley's block centre).
        // Captured once so beginMask + endMask use the same matrix.
        Matrix4f cubeMat = null;
        if (masking) {
            // Flush prior content so unrelated geometry isn't affected by our mask.
            if (buffer instanceof MultiBufferSource.BufferSource bs) bs.endBatch();

            // Pulley world position relative to be.getBlockPos():
            //   Retract (extendPhantom=false): pulley = entity + 1 * pushDir
            //   Extend  (extendPhantom=true):  pulley = entity - 2 * pushDir
            Direction dir = be.getDirection();
            int dist = mpbe.isExtendPhantom() ? -2 : 1;

            pose.pushPose();
            pose.translate(0.5 + dist * dir.getStepX(),
                           0.5 + dist * dir.getStepY(),
                           0.5 + dist * dir.getStepZ());
            cubeMat = new Matrix4f(pose.last().pose());
            pose.popPose();

            DepthMaskUtils.beginMask(cubeMat, mode);
        }

        // Carried block (vanilla path), queued into buffer source.
        super.render(be, partialTick, pose, buffer, packedLight, packedOverlay);

        // Leading phantom (queued into buffer source).
        if (mpbe != null) {
            BlockState leading = mpbe.getLeadingState();
            if (leading != null && !leading.isAir()) {
                Level level = mpbe.getLevel();
                if (level != null) {
                    Direction dir = be.getDirection();
                    float progress = be.getProgress(partialTick);
                    float coeff = mpbe.isExtendPhantom() ? (progress - 2.0F) : progress;
                    pose.pushPose();
                    pose.translate(coeff * dir.getStepX(), coeff * dir.getStepY(), coeff * dir.getStepZ());
                    BlockPos pos = be.getBlockPos();
                    RenderType type = ItemBlockRenderTypes.getMovingBlockRenderType(leading);
                    VertexConsumer vc = buffer.getBuffer(type);
                    blockRenderer.getModelRenderer().tesselateBlock(
                            level, blockRenderer.getBlockModel(leading), leading, pos, pose, vc,
                            false, RandomSource.create(), leading.getSeed(pos), packedOverlay);
                    pose.popPose();
                }
            }
        }

        if (masking) {
            // Rasterize the carried block + phantom against our mask...
            if (buffer instanceof MultiBufferSource.BufferSource bs) bs.endBatch();
            // ...then tear the mask down (clears stencil if applicable).
            DepthMaskUtils.endMask(cubeMat, mode);
        }
    }
}
