package net.mehvahdjukaar.supplementaries.world.structures;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

//@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FeaturesHandler {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Supplementaries.MOD_ID);

    public static final RegistryObject<Feature<NoFeatureConfig>> ROAD_SIGN = FEATURES.register("road_sign_struture",
            ()-> new RoadSignFeature(NoFeatureConfig.CODEC));



    //@SubscribeEvent
    public static void addFeatureToBiomes(BiomeLoadingEvent event) {
        if(!ServerConfigs.spawn.EXPERIMENTAL_ROAD_SIGN.get())return;
        //TODO: try to restrict registration here only to overworld and only if villages generate
        //Biome biome = ForgeRegistries.BIOMES.getValue(event.getName());
        //new StructureSeparationSettings(25, 10, 34222645)
        //.range(256).square().count(50)
        event.getGeneration().getFeatures(GenerationStage.Decoration.SURFACE_STRUCTURES).add(() -> ROAD_SIGN.get()
                .configured(IFeatureConfig.NONE)
                .decorated(Features.Placements.HEIGHTMAP_SQUARE)
                .range(256).chance(100)


        );

    }






}
