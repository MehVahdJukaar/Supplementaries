package net.mehvahdjukaar.supplementaries.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

public record LocatedStructure(BlockPos position, Holder<Structure> structure,
                               @Nullable StructureStart start, double distSqrt) {
    public static LocatedStructure relativeTo(BlockPos structurePos, Holder<Structure> structure,
                                              @Nullable StructureStart start, BlockPos center) {
        return new LocatedStructure(structurePos, structure, start,
                center.distSqr(structurePos));
    }

    @Override
    public String toString() {
        return structure.getRegisteredName() + " at " + position + ", dist = " + Math.sqrt(distSqrt);
    }
}
