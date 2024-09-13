package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.amendments.common.entity.FallingLanternEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class AmendmentsCompat {

    public static boolean canSurviveCeilingAndMaybeFall(BlockState state, BlockPos pos, LevelReader level) {
        return FallingLanternEntity.canSurviveCeilingAndMaybeFall(state, pos, level);
    }
}
