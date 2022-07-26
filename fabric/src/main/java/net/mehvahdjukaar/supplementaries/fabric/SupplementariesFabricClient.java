package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MeshBuilderImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.moonlight.fabric.MoonlightFabric;
import net.mehvahdjukaar.moonlight.fabric.MoonlightFabricClient;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.level.ItemLike;

public class SupplementariesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
    }

    public static void initClient(){

        ClientRegistry.init();
        ClientRegistry.setup();

        registerISTER(ModRegistry.FLUTE_ITEM.get());
        registerISTER(ModRegistry.CAGE_ITEM.get());
        registerISTER(ModRegistry.JAR_ITEM.get());
        registerISTER(ModRegistry.BLACKBOARD_ITEM.get());
        registerISTER(ModRegistry.BUBBLE_BLOCK_ITEM.get());
        ModRegistry.FLAGS.values().forEach(f -> registerISTER(f.get()));
    }

    private static void registerISTER(ItemLike itemLike) {
        ((ICustomItemRendererProvider) itemLike.asItem()).registerFabricRenderer();
    }


}
