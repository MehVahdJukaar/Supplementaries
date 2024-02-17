package net.mehvahdjukaar.supplementaries.common.block.faucet;


import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import static net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager.prepareToTransferBottle;

class BeehiveInteraction implements IFaucetBlockSource, IFaucetBlockTarget {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockState state,
                                      @Nullable FaucetBlockTile.FillAction fillAction) {

        if (state.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
            if (state.getValue(BlockStateProperties.LEVEL_HONEY) == 5) {
                prepareToTransferBottle(faucetTank, BuiltInSoftFluids.HONEY.getHolder());
                if (fillAction == null) return InteractionResult.SUCCESS;
                if (fillAction.tryExecute()) {
                    level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_HONEY, 0), 3);
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
        if (fluid.is(BuiltInSoftFluids.HONEY.get()) && state.hasProperty(BlockStateProperties.LEVEL_HONEY)) {
            int h = state.getValue(BlockStateProperties.LEVEL_HONEY);
            if (h == 0) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL_HONEY, 5), 3);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}

