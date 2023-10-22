package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager.prepareToTransferBucket;

class PowderSnowCauldronInteraction implements IFaucetBlockSource, IFaucetBlockTarget {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockState state, FaucetBlockTile.FillAction fillAction) {
        if (state.is(Blocks.POWDER_SNOW_CAULDRON)) {
            int waterLevel = state.getValue(BlockStateProperties.LEVEL_CAULDRON);
            if (waterLevel == 3) {
                prepareToTransferBucket(faucetTank, BuiltInSoftFluids.POWDERED_SNOW.get());
                if (fillAction == null) return InteractionResult.SUCCESS;
                if (fillAction.tryExecute()) {
                    level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public int getTransferCooldown() {
        return IFaucetBlockSource.super.getTransferCooldown() * 4;
    }

    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
        if (state.is(Blocks.CAULDRON) && faucetTank.getFluid() == BuiltInSoftFluids.POWDERED_SNOW.get()) {
            if (faucetTank.getCount() == 5) {
                level.setBlock(pos, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, 3), 3);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}

