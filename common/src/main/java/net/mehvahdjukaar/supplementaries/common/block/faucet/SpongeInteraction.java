package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

class SpongeInteraction implements FaucetTarget.BlState {

    @Override
    public Integer fill(Level level, BlockPos pos, BlockState target, SoftFluidStack fluid, int minAmount) {
        if (target.getBlock() == Blocks.SPONGE) {
            return minAmount;
        }
        return 0;
    }
}
