package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.FluidsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

class ForgeFluidTankInteraction implements FaucetSource.Tile, FaucetTarget.Tile {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockEntity tile, Direction dir,
                                      @Nullable FaucetBlockTile.FillAction fillAction) {
        if (FluidsUtil.tryExtractFromFluidHandler(tile, tile.getBlockState().getBlock(), dir, faucetTank, fillAction)) {
            if (fillAction == null) return InteractionResult.CONSUME;
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockEntity tile) {
        return FluidsUtil.tryFillFluidTank(tile, faucetTank) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public SoftFluidStack getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockEntity source) {
        return null;
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockEntity source, int amount) {

    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockEntity target, SoftFluidStack fluid) {
        return null;
    }
}

