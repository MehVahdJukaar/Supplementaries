package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EndermanSkullBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;


public class EndermanHeadItemRenderer extends ItemStackRenderer {


    private EndermanSkullBlockTile dummyTile;

    public EndermanHeadItemRenderer() {
        super();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType,
                             PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int combinedOverlayIn) {

        if (dummyTile == null) {
            dummyTile = new EndermanSkullBlockTile(BlockPos.ZERO, ModRegistry.ENDERMAN_SKULL_BLOCK.get().defaultBlockState());
        }

        poseStack.translate(1, 0, 1);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(dummyTile, poseStack, bufferSource, packedLight, combinedOverlayIn);
    }
}