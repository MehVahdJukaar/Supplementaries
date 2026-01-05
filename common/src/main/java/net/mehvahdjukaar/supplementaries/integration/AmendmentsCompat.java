package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.amendments.common.block.ModCauldronBlock;
import net.mehvahdjukaar.amendments.common.entity.FallingLanternEntity;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

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


    public static BlockState fillCauldronWithFluid(Level level, BlockPos pos, BlockState state, FluidState fluidState) {
        SoftFluidStack fluidStack = SoftFluidStack.fromFluid(fluidState);
        if (state.getBlock() == Blocks.CAULDRON) {
            return CauldronConversion.getNewState(pos, level, fluidStack);
        } else if (state.getBlock() instanceof ModCauldronBlock mc) {
            if (mc.isFull(state)) return state;
            if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te &&
                    te.getSoftFluidTank().addFluid(fluidStack, false) != 0) {
                te.setChanged();
            }
        }
        return state;
    }
}

