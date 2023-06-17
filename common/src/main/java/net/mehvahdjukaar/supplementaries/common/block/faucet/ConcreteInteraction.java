package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SugarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.state.BlockState;

class ConcreteInteraction implements IFaucetBlockTarget {

    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
        //sugar is instance of concrete...
        SoftFluid softFluid = faucetTank.getFluid();
        if (softFluid == BuiltInSoftFluids.WATER.get()) {
            if (state.getBlock() instanceof SugarBlock) {
                level.blockEvent(pos, state.getBlock(), 1, 0);
                return InteractionResult.SUCCESS;
            }
            if (state.getBlock() instanceof ConcretePowderBlock cp) {
                level.setBlock(pos, cp.concrete, 3);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
