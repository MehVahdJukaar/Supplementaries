package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.amendments.common.entity.FallingLanternEntity;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class AmendmentsCompat {

    public static boolean canSurviveCeilingAndMaybeFall(BlockState state, BlockPos pos, LevelReader level) {
        return FallingLanternEntity.canSurviveCeilingAndMaybeFall(state, pos, level);
    }

    public static boolean has3DSlimeballRenderer(){
        return ClientConfigs.SLIMEBALL_3D.get();
    }

    public static boolean hasThrowableFireCharge(){
        return CommonConfigs.THROWABLE_FIRE_CHARGES.get();
    }
}

