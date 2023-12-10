package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

class MudInteraction implements IFaucetBlockTarget {


    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
        if (state.is(Blocks.DIRT)) {
            SoftFluid softFluid = faucetTank.getFluid();
            if (softFluid == BuiltInSoftFluids.WATER.get()) {
                level.setBlock(pos, Blocks.MUD.defaultBlockState(), 3);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
