package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

//consume to finish current group
class SoftFluidTankInteraction implements
        IFaucetTileSource, IFaucetTileTarget {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockEntity tile, Direction dir,
                                      @Nullable FaucetBlockTile.FillAction fillAction) {
        if (tile instanceof ISoftFluidTankProvider holder && holder.canInteractWithSoftFluidTank()) {
            SoftFluidTank fluidHolder = holder.getSoftFluidTank();
            faucetTank.copy(fluidHolder);
            faucetTank.setCount(2);
            if (fillAction == null) return InteractionResult.CONSUME;
            if (fillAction.tryExecute()) {
                fluidHolder.shrink(1);
                tile.setChanged();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockEntity tile) {
        if (tile instanceof ISoftFluidTankProvider holder) {
            SoftFluidTank tank = holder.getSoftFluidTank();
            boolean result = faucetTank.tryTransferFluid(tank, faucetTank.getCount() - 1);
            if (result) {
                tile.setChanged();
                faucetTank.fillCount();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}

