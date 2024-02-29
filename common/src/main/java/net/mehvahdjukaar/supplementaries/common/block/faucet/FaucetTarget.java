package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface FaucetTarget<T> {

    // null for pass, non null for success, 0 for fail
    Integer fill(Level level, BlockPos pos, T target, SoftFluidStack fluid, int minAmount);

    interface BlState extends FaucetTarget<BlockState> {
    }

    interface Tile extends FaucetTarget<BlockEntity> {
    }

    interface Fluid extends FaucetTarget<FluidState> {
    }

}
