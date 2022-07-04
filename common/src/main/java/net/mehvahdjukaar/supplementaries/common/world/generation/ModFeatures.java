package net.mehvahdjukaar.supplementaries.common.world.generation;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.world.generation.structure.RoadSignFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(
            ForgeRegistries.FEATURES, Supplementaries.MOD_ID);

    //feature spawned by the structure
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ROAD_SIGN = FEATURES.register(
            "road_sign_feature", () -> new RoadSignFeature(NoneFeatureConfiguration.CODEC));

}
