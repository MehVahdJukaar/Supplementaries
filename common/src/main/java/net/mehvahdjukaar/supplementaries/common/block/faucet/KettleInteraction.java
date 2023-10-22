package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.integration.FarmersRespriteCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager.prepareToTransferBottle;

class KettleInteraction implements IFaucetBlockSource, IFaucetBlockTarget {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockState state, FaucetBlockTile.FillAction fillAction) {
        if (FarmersRespriteCompat.isKettle(state)) {
            var p = FarmersRespriteCompat.getWaterLevel();
            int waterLevel = state.getValue(p);
            if (waterLevel > 0) {

                prepareToTransferBottle(faucetTank, BuiltInSoftFluids.WATER.get());
                if (fillAction == null) return InteractionResult.SUCCESS;
                if (fillAction.tryExecute()) {
                    level.setBlock(pos, state.setValue(p,
                            waterLevel - 1), 3);

                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockState state) {
        if (FarmersRespriteCompat.isKettle(state)) {
            SoftFluid softFluid = faucetTank.getFluid();
            if (softFluid == BuiltInSoftFluids.WATER.get()) {
                var p = FarmersRespriteCompat.getWaterLevel();
                int levels = state.getValue(p);
                if (levels < 3) {
                    level.setBlock(pos, state.setValue(p, levels + 1), 3);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }
}
