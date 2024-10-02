package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.supplementaries.common.utils.ItemsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ContainerItemInteraction implements FaucetItemSource {

    @Override
    public ItemStack tryExtractItem(Level level, BlockPos pos, BlockState state, Direction direction, @Nullable BlockEntity tile) {
        if (tile != null) {
            return ItemsUtil.tryExtractingItem(level, direction, pos, tile);
        }
        return ItemStack.EMPTY;
    }

}
