package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidConsumer;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

class SoftFluidProviderInteraction implements FaucetTarget.BlState, FaucetSource.BlState {

    @Override
    public SoftFluidStack getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockState state) {
        if (state.getBlock() instanceof ISoftFluidProvider p) {
            return p.getProvidedFluid(level, state, pos);
        }
        return SoftFluidStack.empty();
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockState state, int amount) {
        if (state.getBlock() instanceof ISoftFluidProvider p) {
            p.consumeProvidedFluid(level, state, pos);
        }
    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockState state, SoftFluidStack fluid) {
        if (state.getBlock() instanceof ISoftFluidConsumer p) {
            return p.tryAcceptingFluid(level, state, pos, fluid) ? fluid.getCount() : 0;
        }
        return null;
    }
}
