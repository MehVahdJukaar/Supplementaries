package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.client.renderers.fabric.DifferentProspectiveItemRenderer;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.level.ItemLike;

public class SupplementariesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
    }


    public static void clientInitAndSetup() {
        ClientPlatformHelper.addClientSetup(ClientRegistry::init);
        ClientPlatformHelper.addClientSetup(SupplementariesFabricClient::fabricSetup);
        ClientPlatformHelper.addClientSetup(ClientRegistry::setup);

    }

    private static void fabricSetup() {
        registerISTER(ModRegistry.CAGE_ITEM.get());
        registerISTER(ModRegistry.JAR_ITEM.get());
        registerISTER(ModRegistry.BLACKBOARD_ITEM.get());
        registerISTER(ModRegistry.BUBBLE_BLOCK_ITEM.get());
        registerISTER(ModRegistry.ENDERMAN_SKULL_ITEM.get());
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
