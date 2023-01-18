package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.VanillaSoftFluids;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager.prepareToTransferBottle;

class HoneyPotInteraction implements IFaucetBlockSource, IFaucetBlockTarget {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockState state, FaucetBlockTile.FillAction fillAction) {

        if (state.hasProperty(ModBlockProperties.HONEY_LEVEL_POT)) {
            if (state.getValue(ModBlockProperties.HONEY_LEVEL_POT) > 0) {
                prepareToTransferBottle(faucetTank, VanillaSoftFluids.HONEY.get());
                if (fillAction.tryExecute()) {
                    level.setBlock(pos, state.setValue(ModBlockProperties.HONEY_LEVEL_POT,
                            state.getValue(ModBlockProperties.HONEY_LEVEL_POT) - 1), 3);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
        var fluid = faucetTank.getFluid();

        if (fluid == VanillaSoftFluids.HONEY.get() && state.hasProperty(ModBlockProperties.HONEY_LEVEL_POT)) {
            int h = state.getValue(ModBlockProperties.HONEY_LEVEL_POT);
            if (h < 4) {
                level.setBlock(pos, state.setValue(ModBlockProperties.HONEY_LEVEL_POT, h + 1), 3);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}

