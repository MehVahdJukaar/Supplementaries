package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.InspirationCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager.prepareToTransferBottle;

class WaterCauldronInteraction implements IFaucetBlockSource, IFaucetBlockTarget {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockState state, FaucetBlockTile.FillAction fillAction) {
        if (state.is(Blocks.WATER_CAULDRON)) {
            int waterLevel = state.getValue(BlockStateProperties.LEVEL_CAULDRON);
            if (waterLevel > 0) {
                if (CompatHandler.INSPIRATIONS) {
                    return InspirationCompat.doCauldronStuff(level.getBlockEntity(pos), faucetTank, fillAction);
                }

                prepareToTransferBottle(faucetTank, BuiltInSoftFluids.WATER.get());
                if (fillAction == null) return InteractionResult.SUCCESS;
                if (fillAction.tryExecute()) {
                    if (waterLevel > 1) {
                        level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_CAULDRON,
                                waterLevel - 1), 3);
                    } else level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof AbstractCauldronBlock) {
            SoftFluid softFluid = faucetTank.getFluid();
            if (CompatHandler.INSPIRATIONS) {
                return InspirationCompat.tryAddFluid(level.getBlockEntity(pos), faucetTank);
            } else if (softFluid == BuiltInSoftFluids.WATER.get()) {
                if (state.is(Blocks.WATER_CAULDRON)) {
                    int levels = state.getValue(BlockStateProperties.LEVEL_CAULDRON);
                    if (levels < 3) {
                        level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_CAULDRON, levels + 1), 3);
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.FAIL;
                } else if (state.is(Blocks.CAULDRON)) {
                    level.setBlock(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, 1), 3);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
