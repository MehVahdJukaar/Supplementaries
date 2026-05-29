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

/**
 * Vanilla piston rendering + a "leading phantom" block one slot ahead in the push direction.
 * The phantom shares the carried block's progress curve, so a rope being consumed by a pulley
 * animates smoothly from its source slot into the pulley body alongside the rest of the chain
 * sliding up. The phantom is purely visual — see {@link MovingPulleyBlockEntity#getLeadingState}.
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
        super.render(be, partialTick, pose, buffer, packedLight, packedOverlay);
        if (!(be instanceof MovingPulleyBlockEntity mpbe)) return;
        BlockState leading = mpbe.getLeadingState();
        if (leading == null || leading.isAir()) return;
        Level level = mpbe.getLevel();
        if (level == null) return;

        Direction dir = be.getDirection();
        float progress = be.getProgress(partialTick);
        // Carried block renders at offset (progress-1)*step relative to entity pos.
        // Retract phantom: one step further along push axis — slides entity_pos → entity_pos+step
        //   → offset = progress * step (one step beyond carried at all times).
        // Extend phantom: one step BEFORE carried's source — slides entity_pos-2*step → entity_pos-step
        //   → offset = (progress - 2) * step (one step behind carried's source).
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
