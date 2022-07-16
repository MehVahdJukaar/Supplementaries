package net.mehvahdjukaar.supplementaries.reg.generation;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.reg.generation.structure.RoadSignFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.function.Supplier;


public class ModFeatures {

    public static void init(){
    }

    //feature spawned by the structure
    public static final Supplier<Feature<NoneFeatureConfiguration>> ROAD_SIGN = RegHelper.registerFeature(
            Supplementaries.res("road_sign_feature"), () -> new RoadSignFeature(NoneFeatureConfiguration.CODEC));

}
