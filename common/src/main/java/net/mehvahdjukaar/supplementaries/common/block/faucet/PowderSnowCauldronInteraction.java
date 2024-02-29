package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

class PowderSnowCauldronInteraction implements FaucetTarget.BlState, FaucetSource.BlState {

    @Override
    public SoftFluidStack getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockState source) {
        if (source.is(Blocks.POWDER_SNOW_CAULDRON)) {
            return new SoftFluidStack(BuiltInSoftFluids.POWDERED_SNOW.getHolder(), source.getValue(LayeredCauldronBlock.LEVEL));
        }
        return SoftFluidStack.empty();
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
    public Integer fill(Level level, BlockPos pos, BlockState state, SoftFluidStack fluid) {
        int amount = fluid.getCount();
        if (state.is(Blocks.CAULDRON)) {
            if (fluid.is(BuiltInSoftFluids.POWDERED_SNOW.get())) {

                int am = Math.min(amount, 3);
                level.setBlockAndUpdate(pos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, am));
                return am;
            }
        }
        if (state.is(Blocks.POWDER_SNOW_CAULDRON)) {
            if (fluid.is(BuiltInSoftFluids.POWDERED_SNOW.get()) &&
                    state.getValue(LayeredCauldronBlock.LEVEL) < 3) {
                int space = 3 - state.getValue(LayeredCauldronBlock.LEVEL);
                int am = Math.min(amount, space);
                level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL,
                        state.getValue(LayeredCauldronBlock.LEVEL) + am));
                return am;
            } else if (fluid.is(BuiltInSoftFluids.WATER.get())) {
                level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.withPropertiesOf(state));
                return 1;
            }
            return 0;
        }
        return null;
    }

}

