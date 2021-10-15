package net.mehvahdjukaar.supplementaries.world.structures;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

import static net.minecraftforge.common.BiomeDictionary.Type.*;

import RegistryObject;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FeaturesHandler {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Supplementaries.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ROAD_SIGN = FEATURES.register("road_sign_feature",
            () -> new RoadSignFeature(NoneFeatureConfiguration.CODEC));


    @SubscribeEvent
    public static void addFeatureToBiomes(BiomeLoadingEvent event) {
        if (ServerConfigs.spawn.WILD_FLAX_ENABLED.get()) {

            ResourceLocation res = event.getName();
            if (res != null) {

                ResourceKey<Biome> key = ResourceKey.create(ForgeRegistries.Keys.BIOMES, res);
                Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);
                if (types.contains(SANDY) && (types.contains(HOT) || types.contains(DRY))) {
                    event.getGeneration().getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION)
                            .add(() -> ConfiguredFeatures.CONFIGURED_WILD_FLAX);
                }

            }
        }

        /*
        event.getGeneration().getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES).add(() -> ROAD_SIGN.get()
                .configured(IFeatureConfig.NONE)
                .decorated(Features.Placements.HEIGHTMAP_SQUARE)
                .range(256).chance(100)


        );
        */

    }


}
