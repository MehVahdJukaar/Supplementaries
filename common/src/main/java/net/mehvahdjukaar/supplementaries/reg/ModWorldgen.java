package net.mehvahdjukaar.supplementaries.reg;


import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.worldgen.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import java.util.function.Supplier;

public class ModWorldgen {

    public static void init() {
    }

    //structure pieces

    public static final Supplier<StructurePieceType> MINESHAFT_ELEVATOR_PIECE = RegHelper.register(
            Supplementaries.res("mineshaft_elevator"), () -> MineshaftElevatorPiece::new, Registries.STRUCTURE_PIECE);

    //structure types

    public static final Supplier<StructureType<RoadSignStructure>> ROAD_SIGN_STRUCTURE = RegHelper.registerStructure(
            Supplementaries.res("road_sign"), RoadSignStructure.Type::new);

    public static final Supplier<StructureType<GalleonStructure>> GALLEON_STRUCTURE = RegHelper.registerStructure(
            Supplementaries.res("galleon"), GalleonStructure.Type::new);

    //feature types

    //feature spawned by the structure
    public static final Supplier<Feature<RoadSignFeature.Config>> ROAD_SIGN_FEATURE = RegHelper.registerFeature(
            Supplementaries.res("road_sign"), () -> new RoadSignFeature(RoadSignFeature.Config.CODEC));

    public static final Supplier<Feature<BasaltAshFeature.Config>> BASALT_ASH_FEATURE = RegHelper.registerFeature(
            Supplementaries.res("surface_scan_random_patch"), () -> new BasaltAshFeature(BasaltAshFeature.Config.CODEC));

    public static final Supplier<Feature<SpawnEntityWithPassengersFeature.Config>> ENTITY_WITH_PASSENGERS_FEATURE = RegHelper.registerFeature(
            Supplementaries.res("spawn_entity_with_passengers"), () -> new SpawnEntityWithPassengersFeature(SpawnEntityWithPassengersFeature.Config.CODEC));


}

