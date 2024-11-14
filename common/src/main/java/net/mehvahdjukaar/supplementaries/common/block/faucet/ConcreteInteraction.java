package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SugarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.state.BlockState;

class ConcreteInteraction implements FaucetTarget.BlState {

    @Override
    public Integer fill(Level level, BlockPos pos, BlockState state, FluidOffer offer) {
        //sugar is instance of concrete...
        if (offer.fluid().is(MLBuiltinSoftFluids.WATER)) {
            if (state.getBlock() instanceof SugarBlock) {
                level.blockEvent(pos, state.getBlock(), 1, 0);
                return 1;
            }
            if (state.getBlock() instanceof ConcretePowderBlock cp) {
                level.setBlock(pos, cp.concrete.defaultBlockState(), 3);
                return offer.minAmount();
            }
        }
        return null;
    }
}
