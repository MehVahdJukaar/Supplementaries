package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;


public class TileDelegateItemRenderer extends ItemStackRenderer {

    private final Supplier<BlockEntity> dummyTile;

    public TileDelegateItemRenderer(Supplier<? extends BlockEntityType<?>> tileSupp, Supplier<? extends Block> blockSupplier) {
        super();
        this.dummyTile = Suppliers.memoize(() -> tileSupp.get()
                .create(BlockPos.ZERO, blockSupplier.get().defaultBlockState()));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType,
                             PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int combinedOverlayIn) {
        poseStack.pushPose();
        poseStack.translate(1, 0, 1);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        Minecraft.getInstance().getBlockEntityRenderDispatcher()
                .renderItem(dummyTile.get(), poseStack, bufferSource, packedLight, combinedOverlayIn);
        poseStack.popPose();
    }
}