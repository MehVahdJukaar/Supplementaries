package net.mehvahdjukaar.supplementaries.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class SupplementariesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
    }

    public static void initClient() {

        ClientRegistry.init();
        ClientRegistry.setup();

        registerISTER(ModRegistry.CAGE_ITEM.get());
        registerISTER(ModRegistry.JAR_ITEM.get());
        registerISTER(ModRegistry.BLACKBOARD_ITEM.get());
        registerISTER(ModRegistry.BUBBLE_BLOCK_ITEM.get());
        BuiltinItemRendererRegistry.INSTANCE.register(ModRegistry.FLUTE_ITEM.get(), new FluteItemRenderer());

        ModRegistry.FLAGS.values().forEach(f -> registerISTER(f.get()));
    }

    private static void registerISTER(ItemLike itemLike) {
        ((ICustomItemRendererProvider) itemLike.asItem()).registerFabricRenderer();
    }


    private static class FluteItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

        @Override
        public void render(ItemStack stack, ItemTransforms.TransformType transform, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
            if (!stack.isEmpty()) {
                ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

                matrixStack.pushPose();
                boolean gui = transform == ItemTransforms.TransformType.GUI || transform == ItemTransforms.TransformType.GROUND || transform == ItemTransforms.TransformType.FIXED;

                BakedModel model;
                if (gui) {
                    model = ClientPlatformHelper.getModel(itemRenderer.getItemModelShaper().getModelManager(), ClientRegistry.FLUTE_2D_MODEL);
                } else {
                    model = ClientPlatformHelper.getModel(itemRenderer.getItemModelShaper().getModelManager(), ClientRegistry.FLUTE_3D_MODEL);
                }
                RenderType rendertype = ItemBlockRenderTypes.getRenderType(stack, true);
                VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(buffer, rendertype, true, stack.hasFoil());
                itemRenderer.renderModelLists(model, stack, light, overlay, matrixStack, vertexconsumer);
                matrixStack.popPose();
            }
        }
    }
}
