package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

//stuff that can be dropped. is not for fluids just items
public interface IFaucetItemSource {

    ItemStack tryExtractItem(Level level, BlockPos pos, BlockState state, Direction direction, @Nullable BlockEntity tile);

}
