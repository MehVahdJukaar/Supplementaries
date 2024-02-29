package net.mehvahdjukaar.supplementaries.common.block.faucet;


import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

class BeehiveInteraction implements FaucetTarget.BlState, FaucetSource.BlState {

    @Override
    public SoftFluidStack getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockState state) {
        if (state.hasProperty(BlockStateProperties.LEVEL_HONEY) && state.getValue(BlockStateProperties.LEVEL_HONEY) == 5) {
            return new SoftFluidStack(BuiltInSoftFluids.HONEY.getHolder());
        }
        return SoftFluidStack.empty();
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockState source, int amount) {
        level.setBlock(pos, source.setValue(BlockStateProperties.LEVEL_HONEY, 0), 3);
    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockState state, SoftFluidStack fluid) {
        if (state.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
            if (fluid.is(BuiltInSoftFluids.HONEY.get()) && fluid.getCount() == 1 &&
                    state.getValue(BlockStateProperties.LEVEL_HONEY) == 0) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_HONEY, 5), 3);
                return 1;
            }
            return 0;
        }
        return null;
    }
}

