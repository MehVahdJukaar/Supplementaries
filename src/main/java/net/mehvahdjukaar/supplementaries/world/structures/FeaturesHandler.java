package net.mehvahdjukaar.supplementaries.world.structures;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.RegistryConfigs;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

import static net.minecraftforge.common.BiomeDictionary.Type.*;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FeaturesHandler {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Supplementaries.MOD_ID);

    public static final RegistryObject<Feature<NoFeatureConfig>> ROAD_SIGN = FEATURES.register("road_sign_feature",
            () -> new RoadSignFeature(NoFeatureConfig.CODEC));


    @SubscribeEvent
    public static void addFeatureToBiomes(BiomeLoadingEvent event) {
        if (RegistryConfigs.reg.WILD_FLAX_ENABLED.get()) {
            ResourceLocation res = event.getName();
            if (res != null) {

                RegistryKey<Biome> key = RegistryKey.create(ForgeRegistries.Keys.BIOMES, res);
                Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(key);
                if (types.contains(SANDY) && (types.contains(HOT) || types.contains(DRY))) {
                    event.getGeneration().getFeatures(GenerationStage.Decoration.VEGETAL_DECORATION)
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
