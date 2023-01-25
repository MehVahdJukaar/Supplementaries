package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public interface IFaucetTileSource {
    InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                               BlockPos pos, BlockEntity tile, Direction dir,
                               @Nullable FaucetBlockTile.FillAction fillAction);

    default int getTransferCooldown() {
        return FaucetBlockTile.COOLDOWN;
    }
}

