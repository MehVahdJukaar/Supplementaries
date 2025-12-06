package net.mehvahdjukaar.supplementaries.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.List;
import java.util.Optional;

//same as random spread but has more exclusion zones
public class RandomSpreadStructurePlacementWithExclusion extends RandomSpreadStructurePlacement {

    private final List<ExclusionZone> exclusionZones;

    public static final MapCodec<RandomSpreadStructurePlacementWithExclusion> CODEC = RecordCodecBuilder.<RandomSpreadStructurePlacementWithExclusion>mapCodec((i) -> i.group(
                    Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(RandomSpreadStructurePlacementWithExclusion::locateOffset),
                    StructurePlacement.FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", RandomSpreadStructurePlacementWithExclusion.FrequencyReductionMethod.DEFAULT).forGetter(RandomSpreadStructurePlacementWithExclusion::frequencyReductionMethod),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(RandomSpreadStructurePlacementWithExclusion::frequency),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(RandomSpreadStructurePlacementWithExclusion::salt),
                    StructurePlacement.ExclusionZone.CODEC.listOf().optionalFieldOf("exclusion_zones", List.of()).forGetter(RandomSpreadStructurePlacementWithExclusion::exclusionZones),
                    Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing),
                    Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation),
                    RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType))
            .apply(i, RandomSpreadStructurePlacementWithExclusion::new)
    ).validate(RandomSpreadStructurePlacementWithExclusion::validate);

    public RandomSpreadStructurePlacementWithExclusion(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, List<ExclusionZone> exclusionZones, int spacing, int separation, RandomSpreadType spreadType) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, Optional.empty(), spacing, separation, spreadType);
        this.exclusionZones = exclusionZones;
    }

    static boolean isPlacementForbidden(ExclusionZone zone, ChunkGeneratorStructureState structureState, int x, int z) {
        return structureState.hasStructureChunkInRange(zone.otherSet(), x, z, zone.chunkCount());
    }

    @Override
    public boolean applyInteractionsWithOtherStructures(ChunkGeneratorStructureState structureState, int x, int z) {
        for (ExclusionZone zone : exclusionZones) {
            if (isPlacementForbidden(zone, structureState, x, z)) {
                return false;
            }
        }
        return true;
    }

    public List<ExclusionZone> exclusionZones() {
        return exclusionZones;
    }

    private static DataResult<RandomSpreadStructurePlacementWithExclusion> validate(RandomSpreadStructurePlacementWithExclusion placement) {
        return placement.spacing() <= placement.separation() ? DataResult.error(() -> "Spacing has to be larger than separation") : DataResult.success(placement);
    }
}
