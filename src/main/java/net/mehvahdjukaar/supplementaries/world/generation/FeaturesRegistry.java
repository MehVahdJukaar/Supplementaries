package net.mehvahdjukaar.supplementaries.world.generation;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.UrnBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class FeaturesRegistry {

    /**
     * Static instance of our structure, so we can reference it and add it to biomes easily.
     */
    //public static final ConfiguredStructureFeature<?, ?> CONFIGURED_WAY_SIGN = StructureRegistry.WAY_SIGN.get().configured(FeatureConfiguration.NONE);


    //helper
    private static RandomPatchConfiguration makeRandomPatch(int tries, int xzSpread, int ySpread, ConfiguredFeature<?, ?> feature, BlockPredicate placementRule) {
        return new RandomPatchConfiguration(tries, xzSpread, ySpread, () -> feature.filtered(placementRule));
    }

    //placed features predicates
    private static final BlockPredicate HAS_WATER_PREDICATE = BlockPredicate.anyOf(
            BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(1, -1, 0)),
            BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(-1, -1, 0)),
            BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(0, -1, 1)),
            BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(0, -1, -1)));

    private static final BlockPredicate SURVIVES_PREDICATE = BlockPredicate.allOf(
            BlockPredicate.ONLY_IN_AIR_PREDICATE,
            BlockPredicate.wouldSurvive(ModRegistry.URN.get().defaultBlockState(), BlockPos.ZERO)
    );

    private static final BlockPredicate FLAX_PLACEMENT = BlockPredicate.allOf(
            BlockPredicate.matchesTag(BlockTags.SAND, new BlockPos(0, -1, 0)),
            SURVIVES_PREDICATE,
            HAS_WATER_PREDICATE
    );
    private static final BlockPredicate URN_PLACEMENT = BlockPredicate.allOf(
            BlockPredicate.ONLY_IN_AIR_PREDICATE,
            BlockPredicate.solid(BlockPos.ZERO.below())
    );

    //configured features

    public static final ConfiguredFeature<RandomPatchConfiguration, ?> WILD_FLAX_PATCH = Feature.RANDOM_PATCH.configured(
            makeRandomPatch(ServerConfigs.spawn.FLAX_PATCH_TRIES.get(), 4, 0,
                    Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(ModRegistry.FLAX_WILD.get()))),
                    FLAX_PLACEMENT));


    public static final ConfiguredFeature<RandomPatchConfiguration, ?> CAVE_URNS_PATCH = Feature.RANDOM_PATCH.configured(
            makeRandomPatch(ServerConfigs.spawn.URN_PATCH_TRIES.get(), 4, 1,
                    Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(
                            BlockStateProvider.simple(ModRegistry.URN.get().defaultBlockState().setValue(UrnBlock.TREASURE, true)))),
                    URN_PLACEMENT));

    //placed features

    public static final PlacedFeature PLACED_WILD_FLAX_PATCH = WILD_FLAX_PATCH.placed(
            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
            RarityFilter.onAverageOnceEvery(ServerConfigs.spawn.FLAX_AVERAGE_EVERY.get()),
            InSquarePlacement.spread(),
            BiomeFilter.biome());


    public static final PlacedFeature PLACED_CAVE_URNS = CAVE_URNS_PATCH.placed(
            HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-16), VerticalAnchor.aboveBottom(64 + 32)),
            CountPlacement.of(ServerConfigs.spawn.URN_PER_CHUNK.get()),
            InSquarePlacement.spread(),
            BiomeFilter.biome());


    public static void setup() {

        //Registry.register(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE,
        //        Supplementaries.res("configured_way_sign"), CONFIGURED_WAY_SIGN);

        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE,
                Supplementaries.res("wild_flax"), WILD_FLAX_PATCH);

        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE,
                Supplementaries.res("cave_urns"), CAVE_URNS_PATCH);


        Registry.register(BuiltinRegistries.PLACED_FEATURE,
                Supplementaries.res("wild_flax"), PLACED_WILD_FLAX_PATCH);

        Registry.register(BuiltinRegistries.PLACED_FEATURE,
                Supplementaries.res("cave_urns"), PLACED_CAVE_URNS);
    }

}
