package net.mehvahdjukaar.supplementaries.forge;

import com.lowdragmc.shimmer.client.postprocessing.PostProcessing;
import com.lowdragmc.shimmer.client.shader.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.supplementaries.common.capabilities.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.utils.CommonUtil;
import net.mehvahdjukaar.supplementaries.mixins.forge.MobBucketItemAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import software.bernie.example.entity.ExtendedRendererEntity;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SuppPlatformStuffImpl {

    public static EntityType<?> getFishType(MobBucketItem bucketItem) {
        return ((MobBucketItemAccessor) bucketItem).invokeGetFishType();
    }

    /**
     * Does not check if its instance of ICapabilityProvider
     * Be sure to provide it with one, or it will fail
     */
    @Nullable
    public static <T> T getForgeCap(Object object, Class<T> capClass) {
        var t = CapabilityHandler.getToken(capClass);
        if (t != null) {
            return (((ICapabilityProvider) object).getCapability(t).resolve()).orElse(null);
        }
        return null;
    }

    @Nullable
    public static BlockState getUnoxidised(Level level, BlockPos pos, BlockState state) {
        Player fp = CommonUtil.getFakePlayer(level);
        fp.setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_AXE.getDefaultInstance());
        Block b = state.getBlock();
        var context = new UseOnContext(fp, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));

        var modified = state;
        modified = b.getToolModifiedState(modified, context, ToolActions.AXE_WAX_OFF, false);
        if (modified == null) modified = state;
        while (true) {
            var newMod = b.getToolModifiedState(modified, context, ToolActions.AXE_SCRAPE, false);

            if (newMod == null || newMod == modified) break;
            else modified = newMod;
        }
        if (modified == state) return null;
        return modified;
    }

    public static void renderBlock(int i, PoseStack poseStack, MultiBufferSource bufferIn, BlockState state, Level level, BlockPos blockPos, BlockRenderDispatcher blockRenderer) {
        // a MultiBufferSource for entity or BlockEntityRenderer
        PoseStack finalStack = RenderUtils.copyPoseStack(poseStack); // we provide a way to copy the poststack
        PostProcessing.BLOOM_UNITY.postEntity(bufferSource -> {
            BakedModel model = blockRenderer.getBlockModel(state);
            for (var renderType : model.getRenderTypes(state, RandomSource.create(i), ModelData.EMPTY)) {
                blockRenderer.getModelRenderer().tesselateBlock(level, model, state, blockPos, finalStack, bufferIn.getBuffer(renderType), false, RandomSource.create(), i,
                        OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
            }
            //.renderBlock(i, finalStack, bufferIn, state, level, blockPos, blockRenderer);
        });
        PostProcessing.BLOOM_UNITY.renderEntityPost(new ProfilerFiller() {
            @Override
            public void startTick() {

            }

            @Override
            public void endTick() {

            }

            @Override
            public void push(String name) {

            }

            @Override
            public void push(Supplier<String> nameSupplier) {

            }

            @Override
            public void pop() {

            }

            @Override
            public void popPush(String name) {

            }

            @Override
            public void popPush(Supplier<String> nameSupplier) {

            }

            @Override
            public void markForCharting(MetricCategory category) {

            }

            @Override
            public void incrementCounter(String counterName, int increment) {

            }

            @Override
            public void incrementCounter(Supplier<String> counterNameSupplier, int increment) {

            }
        });

    }


}
