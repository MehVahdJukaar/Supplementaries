package net.mehvahdjukaar.supplementaries.reg;


import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.blocks.UrnBlock;
import net.mehvahdjukaar.supplementaries.common.worldgen.*;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.function.Supplier;

public class ModWorldgenRegistry {

    public static void init() {
    }

    //structure pieces

    public static final Supplier<StructurePieceType> MINESHAFT_ELEVATOR = RegHelper.register(
            Supplementaries.res("mineshaft_elevator"), () -> MineshaftElevatorPiece::new, Registry.STRUCTURE_PIECE);

    //structure types

    public static final Supplier<StructureType<WaySignStructure>> WAY_SIGN = RegHelper.registerStructure(
            Supplementaries.res("way_sign"), WaySignStructure.Type::new);


    //feature types

    //feature spawned by the structure
    public static final Supplier<Feature<NoneFeatureConfiguration>> ROAD_SIGN_FEATURE = RegHelper.registerFeature(
            Supplementaries.res("road_sign_feature"), () -> new RoadSignFeature(NoneFeatureConfiguration.CODEC));

    public static final Supplier<Feature<BasaltAshFeature.Config>> BASALT_ASH_FEATURE = RegHelper.registerFeature(
            Supplementaries.res("layered_blocks"), () -> new BasaltAshFeature(BasaltAshFeature.Config.CODEC));


    //modifiers

    public static final Supplier<PlacementModifierType<CaveFilter>> CAVE_MODIFIER = RegHelper.register(
            Supplementaries.res("cave"), CaveFilter.Type::new, Registry.PLACEMENT_MODIFIERS);


    //placed features predicates

    private static final BlockPredicate HAS_WATER_PREDICATE = BlockPredicate.anyOf(
            BlockPredicate.matchesFluids(new BlockPos(1, -1, 0), List.of(Fluids.WATER, Fluids.FLOWING_WATER)),
            BlockPredicate.matchesFluids(new BlockPos(-1, -1, 0), List.of(Fluids.WATER, Fluids.FLOWING_WATER)),
            BlockPredicate.matchesFluids(new BlockPos(0, -1, 1), List.of(Fluids.WATER, Fluids.FLOWING_WATER)),
            BlockPredicate.matchesFluids(new BlockPos(0, -1, -1), List.of(Fluids.WATER, Fluids.FLOWING_WATER)));


    //configured features

    public static final RegSupplier<ConfiguredFeature<RandomPatchConfiguration, Feature<RandomPatchConfiguration>>> WILD_FLAX_PATCH =
            RegHelper.registerConfiguredFeature(Supplementaries.res("wild_flax"), () -> Feature.RANDOM_PATCH,
                    () -> getPatchConfiguration(
                            CommonConfigs.Spawns.FLAX_PATCH_TRIES.get(),
                            4, 0,
                            new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
                                    BlockStateProvider.simple(ModRegistry.FLAX_WILD.get()))),

                            BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                                    BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                    BlockPredicate.wouldSurvive(ModRegistry.FLAX_WILD.get().defaultBlockState(), BlockPos.ZERO),
                                    HAS_WATER_PREDICATE))));

    public static final RegSupplier<ConfiguredFeature<RandomPatchConfiguration, Feature<RandomPatchConfiguration>>> CAVE_URNS_PATCH =
            RegHelper.registerConfiguredFeature(Supplementaries.res("cave_urns"), () -> Feature.RANDOM_PATCH,
                    () -> getPatchConfiguration(
                            CommonConfigs.Spawns.URN_PATCH_TRIES.get(),
                            4, 1,
                            new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
                                    BlockStateProvider.simple(ModRegistry.URN.get().defaultBlockState().setValue(UrnBlock.TREASURE, true)))),

                            BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                                    BlockPredicate.ONLY_IN_AIR_PREDICATE,
                                    BlockPredicate.solid(BlockPos.ZERO.below())))));

    public static final RegSupplier<ConfiguredFeature<BasaltAshFeature.Config, Feature<BasaltAshFeature.Config>>> BASALT_ASH_PATCH =
            RegHelper.registerConfiguredFeature(Supplementaries.res("basalt_ash"),
                    () -> new ConfiguredFeature<>(BASALT_ASH_FEATURE.get(),
                            new BasaltAshFeature.Config(CommonConfigs.Spawns.BASALT_ASH_TRIES.get(),6, 6)));

    public static final RegSupplier<ConfiguredFeature<NoneFeatureConfiguration, Feature<NoneFeatureConfiguration>>> ROAD_SIGN =
            RegHelper.registerConfiguredFeature(Supplementaries.res("road_sign"),
                    ROAD_SIGN_FEATURE, () -> FeatureConfiguration.NONE);


    //placed features

    public static final RegSupplier<PlacedFeature> PLACED_WILD_FLAX_PATCH =
            RegHelper.registerPlacedFeature(Supplementaries.res("wild_flax"),
                    WILD_FLAX_PATCH,
                    () -> List.of(
                            PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                            RarityFilter.onAverageOnceEvery(CommonConfigs.Spawns.FLAX_AVERAGE_EVERY.get()),
                            InSquarePlacement.spread(),
                            BiomeFilter.biome()));

    public static final RegSupplier<PlacedFeature> PLACED_CAVE_URNS =
            RegHelper.registerPlacedFeature(Supplementaries.res("cave_urns"),
                    CAVE_URNS_PATCH,
                    () -> List.of(
                            HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(-16), VerticalAnchor.aboveBottom(64 + 32)),
                            CountPlacement.of(CommonConfigs.Spawns.URN_PER_CHUNK.get()),
                            InSquarePlacement.spread(),
                            CaveFilter.BELOW_SURFACE,
                            BiomeFilter.biome()));

    public static final RegSupplier<PlacedFeature> PLACED_BASALT_ASH =
            RegHelper.registerPlacedFeature(Supplementaries.res("basalt_ash"),
                    BASALT_ASH_PATCH,
                    () -> List.of(
                            HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(29), VerticalAnchor.aboveBottom(102)),
                            CountPlacement.of(CommonConfigs.Spawns.BASALT_ASH_PER_CHUNK.get()),
                            InSquarePlacement.spread(),
                            BiomeFilter.biome()));

    public static final RegSupplier<PlacedFeature> PLACED_ROAD_SIGN =
            RegHelper.registerPlacedFeature(Supplementaries.res("road_sign"), ROAD_SIGN, List::of);


    //helper
    private static RandomPatchConfiguration getPatchConfiguration(int tries, int xzSpread, int ySpread, ConfiguredFeature<?, ?> feature, PlacementModifier... placementRule) {
        return new RandomPatchConfiguration(tries, xzSpread, ySpread, PlacementUtils.inlinePlaced(Holder.direct(feature), placementRule));
    }


}

