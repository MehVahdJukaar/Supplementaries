package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.VanillaSoftFluids;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import static net.mehvahdjukaar.supplementaries.common.block.faucet.FaucetBehaviorsManager.prepareToTransferBottle;

class WaterBlockInteraction implements IFaucetFluidSource {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, FluidState fluidState, FaucetBlockTile.FillAction fillAction) {

        if (fluidState.getType() == Fluids.WATER) {
            //Unlimited water!!
            prepareToTransferBottle(faucetTank, VanillaSoftFluids.WATER.get());
            if (fillAction == null || fillAction.tryExecute()) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

}
