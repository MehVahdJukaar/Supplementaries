package net.mehvahdjukaar.supplementaries.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.QuiverLayer;
import net.mehvahdjukaar.supplementaries.client.renderers.fabric.DifferentProspectiveItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.fabric.QuiverArrowSelectGuiImpl;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class SupplementariesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
    }


    @SuppressWarnings("unchecked")
    public static void initClient() {

        ClientRegistry.init();
        ClientRegistry.setup();

        registerISTER(ModRegistry.CAGE_ITEM.get());
        registerISTER(ModRegistry.JAR_ITEM.get());
        registerISTER(ModRegistry.BLACKBOARD_ITEM.get());
        registerISTER(ModRegistry.BUBBLE_BLOCK_ITEM.get());
        BuiltinItemRendererRegistry.INSTANCE.register(ModRegistry.FLUTE_ITEM.get(),
                new DifferentProspectiveItemRenderer(ClientRegistry.FLUTE_2D_MODEL, ClientRegistry.FLUTE_3D_MODEL));
        BuiltinItemRendererRegistry.INSTANCE.register(ModRegistry.QUIVER_ITEM.get(),
                new DifferentProspectiveItemRenderer(ClientRegistry.QUIVER_2D_MODEL, ClientRegistry.QUIVER_3D_MODEL));

        ModRegistry.FLAGS.values().forEach(f -> registerISTER(f.get()));
    }


    private static void registerISTER(ItemLike itemLike) {
        ((ICustomItemRendererProvider) itemLike.asItem()).registerFabricRenderer();
    }



}
