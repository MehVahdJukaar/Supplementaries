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
    public SoftFluidStack getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        if (tile instanceof ISoftFluidTankProvider holder && holder.canInteractWithSoftFluidTank()) {
            return holder.getSoftFluidTank().getFluid().copy();
        }
        return SoftFluidStack.empty();
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
    public Integer fill(Level level, BlockPos pos, BlockEntity tile, SoftFluidStack fluid) {
        if (tile instanceof ISoftFluidTankProvider holder && holder.canInteractWithSoftFluidTank()) {
            SoftFluidTank tank = holder.getSoftFluidTank();
            if(tank.addFluid(fluid.copyWithCount(1))){
                tile.setChanged();
                return 1;
            }return 0;
        }
        return null;
    }
}

