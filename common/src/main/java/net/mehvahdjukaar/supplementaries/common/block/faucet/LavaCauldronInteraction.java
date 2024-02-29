package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

class LavaCauldronInteraction implements FaucetTarget.BlState, FaucetSource.BlState {

    @Override
    public FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockState source) {
        if (source.is(Blocks.LAVA_CAULDRON)) {
            return FluidOffer.of(BuiltInSoftFluids.LAVA.getHolder(), SoftFluid.BUCKET_COUNT, SoftFluid.BUCKET_COUNT);
        }
        return null;
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockState source, int amount) {
        level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockState state, SoftFluidStack fluid, int minAmount) {
        if (state.is(Blocks.CAULDRON) && fluid.is(BuiltInSoftFluids.LAVA.get())
                && fluid.getCount() >= SoftFluid.BUCKET_COUNT) {
            level.setBlock(pos, Blocks.LAVA_CAULDRON.defaultBlockState(), 3);
            return SoftFluid.BUCKET_COUNT;
        }
        return null;
    }
}

