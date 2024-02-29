package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface FaucetSource<T> {

    // gets maximum fluid that this behavior can provide as well as the max amount of it
    SoftFluidStack getProvidedFluid(Level level, BlockPos pos, Direction dir, T source);

    void drain(Level level, BlockPos pos, Direction dir, T source, int amount);

    interface Tile extends FaucetSource<BlockEntity> {
    }

    interface BlState extends FaucetSource<BlockState> {
    }

    interface Fluid extends FaucetSource<FluidState> {
    }

}

