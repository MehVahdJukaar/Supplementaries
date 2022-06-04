package net.mehvahdjukaar.supplementaries.common.world.generation;

import net.mehvahdjukaar.supplementaries.common.block.blocks.UrnBlock;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class ModConfiguredFeatures {

    //helper
    private static RandomPatchConfiguration getPatchConfiguration(int tries, int xzSpread, int ySpread, ConfiguredFeature<?, ?> feature, PlacementModifier placementRule) {
        return new RandomPatchConfiguration(tries, xzSpread, ySpread, PlacementUtils.inlinePlaced(Holder.direct(feature), placementRule));
    }

    //placed features predicates
    private static final BlockPredicate HAS_WATER_PREDICATE = BlockPredicate.anyOf(
            BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(1, -1, 0)),
            BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(-1, -1, 0)),
            BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(0, -1, 1)),
            BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(0, -1, -1)));

    private static final PlacementModifier FLAX_PLACEMENT = BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
            BlockPredicate.ONLY_IN_AIR_PREDICATE,
            BlockPredicate.wouldSurvive(ModRegistry.FLAX_WILD.get().defaultBlockState(), BlockPos.ZERO),
            HAS_WATER_PREDICATE
    ));
    private static final PlacementModifier URN_PLACEMENT = BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
            BlockPredicate.ONLY_IN_AIR_PREDICATE,
            BlockPredicate.solid(BlockPos.ZERO.below())
    ));


    //configured features

    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> WILD_FLAX_PATCH =
            FeatureUtils.register("supplementaries:wild_flax", Feature.RANDOM_PATCH,
                    getPatchConfiguration(ServerConfigs.spawn.FLAX_PATCH_TRIES.get(), 4, 0,
                            new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
                                    BlockStateProvider.simple(ModRegistry.FLAX_WILD.get()))),
                            FLAX_PLACEMENT));


    public static final Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> CAVE_URNS_PATCH =
            FeatureUtils.register("supplementaries:cave_urns", Feature.RANDOM_PATCH,
                    getPatchConfiguration(ServerConfigs.spawn.URN_PATCH_TRIES.get(), 4, 1,
                            new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
                                    BlockStateProvider.simple(ModRegistry.URN.get().defaultBlockState().setValue(UrnBlock.TREASURE, true)))),
                            URN_PLACEMENT));

    public static final Holder<ConfiguredFeature<NoneFeatureConfiguration,?>> ROAD_SIGN =
            FeatureUtils.register("supplementaries:road_sign",ModFeatures.ROAD_SIGN.get(), FeatureConfiguration.NONE);


}
