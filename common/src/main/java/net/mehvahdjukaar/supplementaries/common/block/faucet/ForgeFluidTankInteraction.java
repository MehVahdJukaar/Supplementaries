package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.FluidsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

class ForgeFluidTankInteraction implements
        IFaucetTileSource, IFaucetTileTarget {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockEntity tile, Direction dir,
                                      @Nullable FaucetBlockTile.FillAction fillAction) {
        if (FluidsUtil.tryExtractFromFluidHandler(tile, tile.getBlockState().getBlock(), dir, faucetTank, fillAction)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockEntity tile) {
        return FluidsUtil.tryFillFluidTank(tile, faucetTank) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}

