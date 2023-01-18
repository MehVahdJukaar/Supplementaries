package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidConsumer;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager.prepareToTransferBottle;

class SoftFluidProviderInteraction implements
        IFaucetBlockSource, IFaucetTileSource, IFaucetTileTarget, IFaucetBlockTarget {

    @Override
    public int getTransferCooldown() {
        return IFaucetBlockSource.super.getTransferCooldown();
    }

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockState state, FaucetBlockTile.FillAction fillAction) {
        return drainGeneric(level, faucetTank, pos, state, fillAction, state.getBlock());
    }

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockEntity tile, Direction dir, FaucetBlockTile.FillAction fillAction) {
        return drainGeneric(level, faucetTank, pos, tile.getBlockState(), fillAction, tile);
    }

    private static InteractionResult drainGeneric(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state, FaucetBlockTile.FillAction fillAction, Object backBlock) {
        if (backBlock instanceof ISoftFluidProvider provider) {
            var stack = provider.getProvidedFluid(level, state, pos);
            prepareToTransferBottle(faucetTank, stack.getFirst(), stack.getSecond());
            if (fillAction.tryExecute()) {
                provider.consumeProvidedFluid(level, state, pos, faucetTank.getFluid(), faucetTank.getNbt(), 1);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
        return tryFillGeneric(level, faucetTank, pos, state, state.getBlock());
    }


    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockEntity tile) {
        return tryFillGeneric(level, faucetTank, pos, tile.getBlockState(), tile);
    }

    public InteractionResult tryFillGeneric(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state, Object object) {
        if (object instanceof ISoftFluidConsumer consumer) {
            return consumer.tryAcceptingFluid(level, state, pos, faucetTank.getFluid(), faucetTank.getNbt(), 1)
                    ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}
