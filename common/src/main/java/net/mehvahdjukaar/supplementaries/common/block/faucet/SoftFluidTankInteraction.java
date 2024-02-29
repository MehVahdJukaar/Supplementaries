package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

//consume to finish current group
class SoftFluidTankInteraction implements FaucetSource.Tile, FaucetTarget.Tile {

    @Override
    public FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        if (tile instanceof ISoftFluidTankProvider holder && holder.canInteractWithSoftFluidTank()) {
            SoftFluidStack fluid = holder.getSoftFluidTank().getFluid();
            if (fluid.isEmpty()) return null;
            return FluidOffer.of(fluid.copy());
        }
        return null;
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockEntity tile, int amount) {
        if (tile instanceof ISoftFluidTankProvider holder && holder.canInteractWithSoftFluidTank()) {
            SoftFluidTank fluidHolder = holder.getSoftFluidTank();
            int am = Math.min(amount, fluidHolder.getFluidCount());
            fluidHolder.getFluid().shrink(am);
            tile.setChanged();
        }
    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockEntity tile, SoftFluidStack fluid, int minAmount) {
        if (tile instanceof ISoftFluidTankProvider holder && holder.canInteractWithSoftFluidTank()) {
            SoftFluidTank tank = holder.getSoftFluidTank();
            if (tank.addFluid(fluid.copyWithCount(minAmount))) {
                tile.setChanged();
                return minAmount;
            }
            return 0;
        }
        return null;
    }
}

