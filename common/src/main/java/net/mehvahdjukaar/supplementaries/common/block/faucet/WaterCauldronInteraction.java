package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

class WaterCauldronInteraction implements FaucetTarget.BlState, FaucetSource.BlState {

    @Override
    public FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockState source) {
        if (source.is(Blocks.WATER_CAULDRON)) {
            return FluidOffer.of(BuiltInSoftFluids.WATER.getHolder(), source.getValue(LayeredCauldronBlock.LEVEL));
        }
        return null;
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockState source, int amount) {
        int am = source.getValue(LayeredCauldronBlock.LEVEL) - amount;
        if (am <= 0) {
            level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
        } else {
            level.setBlockAndUpdate(pos, source.setValue(LayeredCauldronBlock.LEVEL, am));
        }
    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockState state, FluidOffer offer) {
        if (state.is(Blocks.CAULDRON)) {
            if (offer.fluid().is(BuiltInSoftFluids.WATER)) {
                int minAmount = offer.minAmount();
                int am = Math.min(minAmount, 3);
                level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, am));
                return minAmount;
            }
        }
        if (state.is(Blocks.WATER_CAULDRON)) {
            SoftFluidStack fluid = offer.fluid();
            if (fluid.is(BuiltInSoftFluids.WATER) &&
                    state.getValue(LayeredCauldronBlock.LEVEL) < 3) {
                int amount = fluid.getCount();

                int space = 3 - state.getValue(LayeredCauldronBlock.LEVEL);
                int am = Math.min(amount, space);
                level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL,
                        state.getValue(LayeredCauldronBlock.LEVEL) + am));
                return Math.max(offer.minAmount(), am);
            }
            return 0;
        }
        return null;
    }
}
