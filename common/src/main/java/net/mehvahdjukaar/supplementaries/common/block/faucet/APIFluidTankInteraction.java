package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.common.utils.FluidsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

class APIFluidTankInteraction implements FaucetSource.Tile, FaucetTarget.Tile {


    @Override
    public FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockEntity source) {
        return FluidsUtil.getFluidInTank(level, pos, dir, source);
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockEntity tile, int amount) {
        FluidsUtil.extractFluidFromTank(tile, dir, amount);
    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockEntity target, FluidOffer offer) {
        return FluidsUtil.fillFluidTank(target, offer);
    }
}

