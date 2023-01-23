package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.VanillaSoftFluids;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager.prepareToTransferBucket;

class LavaCauldronInteraction implements IFaucetBlockSource, IFaucetBlockTarget {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockState state, FaucetBlockTile.FillAction fillAction) {
        if (state.is(Blocks.LAVA_CAULDRON)) {
            prepareToTransferBucket(faucetTank, VanillaSoftFluids.LAVA.get());
            if (fillAction.tryExecute()) {
                level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public int getTransferCooldown() {
        return IFaucetBlockSource.super.getTransferCooldown() * 4;
    }


    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
        if (state.is(Blocks.CAULDRON) && faucetTank.getFluid() == VanillaSoftFluids.LAVA.get()) {
            if (faucetTank.getCount() == 5) {
                level.setBlock(pos, Blocks.LAVA_CAULDRON.defaultBlockState(), 3);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}

