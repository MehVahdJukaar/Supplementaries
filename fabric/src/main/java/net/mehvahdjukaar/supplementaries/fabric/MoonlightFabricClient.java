package net.mehvahdjukaar.supplementaries.fabric;

import net.mehvahdjukaar.supplementaries.SupplementariesClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Function;

public class MoonlightFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SupplementariesClient.initClient();
        //TODO: add ISTERS
    }


}
