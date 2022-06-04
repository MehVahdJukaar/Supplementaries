package net.mehvahdjukaar.supplementaries.common.world.generation;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class ModPlacedFeatures {

    public static void init(){};

    //placed features

    public static final Holder<PlacedFeature> PLACED_WILD_FLAX_PATCH = PlacementUtils.register(
            "supplementaries:wild_flax", ModConfiguredFeatures.WILD_FLAX_PATCH, List.of(
                    PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                    RarityFilter.onAverageOnceEvery(ServerConfigs.spawn.FLAX_AVERAGE_EVERY.get()),
                    InSquarePlacement.spread(),
                    BiomeFilter.biome()));


    public static final Holder<PlacedFeature> PLACED_CAVE_URNS = PlacementUtils.register(
            "supplementaries:cave_urns", ModConfiguredFeatures.CAVE_URNS_PATCH, List.of(
                    HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(-16), VerticalAnchor.aboveBottom(64 + 32)),
                    CountPlacement.of(ServerConfigs.spawn.URN_PER_CHUNK.get()),
                    InSquarePlacement.spread(),
                    CaveFilter.BELOW_SURFACE,
                    BiomeFilter.biome()));

    public static final Holder<PlacedFeature> PLACED_ROAD_SIGN = PlacementUtils.register(
            "supplementaries:road_sign", ModConfiguredFeatures.ROAD_SIGN, List.of());

}
