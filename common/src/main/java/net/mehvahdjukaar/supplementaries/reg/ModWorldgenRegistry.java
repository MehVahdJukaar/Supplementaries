package net.mehvahdjukaar.supplementaries.reg;


import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.worldgen.*;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

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
    public static final Supplier<Feature<RoadSignFeature.Config>> ROAD_SIGN_FEATURE = RegHelper.registerFeature(
            Supplementaries.res("road_sign_feature"), () -> new RoadSignFeature(RoadSignFeature.Config.CODEC));

    public static final Supplier<Feature<BasaltAshFeature.Config>> BASALT_ASH_FEATURE = RegHelper.registerFeature(
            Supplementaries.res("surface_scan_random_patch"), () -> new BasaltAshFeature(BasaltAshFeature.Config.CODEC));

    public static final Supplier<PlacementModifierType<CaveFilter>> CAVE_MODIFIER = RegHelper.register(
            Supplementaries.res("cave"), CaveFilter.Type::new, Registry.PLACEMENT_MODIFIERS);


}
