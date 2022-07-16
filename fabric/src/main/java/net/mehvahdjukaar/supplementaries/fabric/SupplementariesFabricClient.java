package net.mehvahdjukaar.supplementaries.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.supplementaries.SupplementariesClient;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.level.ItemLike;

public class SupplementariesFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SupplementariesClient.initClient();

        registerISTER(ModRegistry.FLUTE_ITEM.get());
        registerISTER(ModRegistry.CAGE_ITEM.get());
        registerISTER(ModRegistry.JAR_ITEM.get());
        registerISTER(ModRegistry.BLACKBOARD_ITEM.get());
        registerISTER(ModRegistry.BUBBLE_BLOCK_ITEM.get());
        ModRegistry.FLAGS.values().forEach(f -> registerISTER(f.get()));
    }

    private void registerISTER(ItemLike itemLike) {
        ((ICustomItemRendererProvider) itemLike.asItem()).registerFabricRenderer();
    }


}
