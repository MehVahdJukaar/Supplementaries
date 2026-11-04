package net.mehvahdjukaar.supplementaries.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.block.TomatoBlock;

public abstract class TomatoLoggedBlock extends TomatoBlock {

    protected TomatoLoggedBlock(Properties properties) {
        super(properties);
    }

    public abstract BlockState getInnerState(BlockState state, LevelAccessor level, BlockPos pos);

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            //we can't just break block or other ropes will react when instead we want to replace with another rope
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            Block.dropResources(state, level, pos, null, null, ItemStack.EMPTY);

            level.setBlockAndUpdate(pos, getInnerState(state, level, pos));
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        level.setBlockAndUpdate(pos, getInnerState(state, level, pos));
    }
}
