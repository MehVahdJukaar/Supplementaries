package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IFaucetItemSource {

    ItemStack tryExtractItem(Level level, BlockPos pos, BlockState state);

    default int getTransferCooldown() {
        return FaucetBlockTile.COOLDOWN;
    }

}
