package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IFaucetBlockSource {
    InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                               BlockPos pos, BlockState state, FaucetBlockTile.FillAction fillAction);

    default int getTransferCooldown() {
        return FaucetBlockTile.COOLDOWN;
    }
}
