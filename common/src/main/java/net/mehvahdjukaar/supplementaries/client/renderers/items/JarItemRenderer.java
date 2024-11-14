package net.mehvahdjukaar.supplementaries.client.renderers.items;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.JarBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.items.components.SoftFluidTankView;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import static net.mehvahdjukaar.supplementaries.client.renderers.tiles.JarBlockTileRenderer.renderFluid;


public class JarItemRenderer extends CageItemRenderer {

    private static final RandomSource RAND = RandomSource.createNewThreadLocalInstance();


    @Override
    public void renderContent(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        super.renderContent(stack, transformType, poseStack, buffer, light, overlay);

        SoftFluidTankView fluidContent = stack.get(ModComponents.SOFT_FLUID_CONTENT.get());
        if (fluidContent != null && !fluidContent.isEmpty()) {
            int count = fluidContent.getCount();
            if (count != 0) {
                int color = fluidContent.getFlowingColor(Minecraft.getInstance().level);
                renderFluid(getVisualHeight(count, 1), color, 0, fluidContent.getFluid().getStillTexture(),
                        poseStack, buffer, light, overlay);
            }
        }
        ItemContainerContents content = stack.get(DataComponents.CONTAINER);
        if (content != null) {
            RAND.setSeed(420);

            var items = content.nonEmptyItems().iterator();
            JarBlockTileRenderer.renderCookies(
                    Minecraft.getInstance().getItemRenderer(),
                    poseStack, buffer, RAND, light, overlay, () -> items.hasNext() ? items.next() : ItemStack.EMPTY);
        }
    }

    private static float getVisualHeight(float count, float maxHeight) {
        return maxHeight * count / (float) CommonConfigs.Functional.JAR_CAPACITY.get();
    }
}

