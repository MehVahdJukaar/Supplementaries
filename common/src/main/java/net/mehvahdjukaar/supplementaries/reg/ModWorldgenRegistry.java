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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
            Supplementaries.res("mineshaft_elevator"), () -> MineshaftElevatorPiece::new, Registries.STRUCTURE_PIECE);

    //structure types

    public static final Supplier<StructureType<WaySignStructure>> WAY_SIGN = RegHelper.registerStructure(
            Supplementaries.res("way_sign"), WaySignStructure.Type::new);

    //feature types

    //feature spawned by the structure
    public static final Supplier<Feature<NoneFeatureConfiguration>> ROAD_SIGN_FEATURE = RegHelper.registerFeature(
            Supplementaries.res("road_sign_feature"), () -> new RoadSignFeature(NoneFeatureConfiguration.CODEC));

    public static final Supplier<Feature<BasaltAshFeature.Config>> BASALT_ASH_FEATURE = RegHelper.registerFeature(
            Supplementaries.res("surface_scan_random_patch"), () -> new BasaltAshFeature(BasaltAshFeature.Config.CODEC));

    //modifiers

    public static final Supplier<PlacementModifierType<CaveFilter>> CAVE_MODIFIER = RegHelper.register(
            Supplementaries.res("cave"), CaveFilter.Type::new, Registries.PLACEMENT_MODIFIER_TYPE);

}

