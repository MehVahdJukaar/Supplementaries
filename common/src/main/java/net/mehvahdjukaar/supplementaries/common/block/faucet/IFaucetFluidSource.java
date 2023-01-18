package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

public interface IFaucetFluidSource {
    InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                               BlockPos pos, FluidState fluidState, FaucetBlockTile.FillAction fillAction);

    default int getTransferCooldown() {
        return FaucetBlockTile.COOLDOWN;
    }
}
