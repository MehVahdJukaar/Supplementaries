package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;


public class FlagItemRenderer extends ItemStackRenderer {

    private static final BlockState state = ModRegistry.FLAGS.get(DyeColor.BLACK).get().defaultBlockState();

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        BannerPatternLayers patterns = stack.get(DataComponents.BANNER_PATTERNS);
        if (patterns != null) {
            matrixStackIn.pushPose();
            matrixStackIn.translate(-0.71875, 0, 0);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
            matrixStackIn.translate(0.5 + 0.0625, 0, 0.5);
            matrixStackIn.mulPose(RotHlpr.Y90);
            FlagBlockTileRenderer.renderPatterns(matrixStackIn, bufferIn, patterns, combinedLightIn,
                    ((FlagItem) stack.getItem()).getColor());

            matrixStackIn.popPose();
        }

    }
}