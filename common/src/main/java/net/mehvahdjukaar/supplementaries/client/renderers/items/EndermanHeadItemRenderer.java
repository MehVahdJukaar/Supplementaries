package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.EndermanSkullBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;


public class EndermanHeadItemRenderer extends ItemStackRenderer {


    private final EndermanSkullBlockTile dummyTile;
    private final BlockEntityRenderDispatcher renderer;

    public EndermanHeadItemRenderer() {
        super();
        this.renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        this.dummyTile = new EndermanSkullBlockTile(BlockPos.ZERO, ModRegistry.ENDERMAN_SKULL_BLOCK.get().defaultBlockState());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType,
                             PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int combinedOverlayIn) {

        poseStack.translate(1,0,1);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        renderer.renderItem(dummyTile, poseStack, bufferSource, packedLight, combinedOverlayIn);
    }
}