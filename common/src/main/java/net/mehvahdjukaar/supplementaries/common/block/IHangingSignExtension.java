package net.mehvahdjukaar.supplementaries.common.block;

import net.mehvahdjukaar.supplementaries.common.block.tiles.SwayingBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface IHangingSignExtension {
    void updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos);

    ModBlockProperties.PostType getLeftAttachment();

    ModBlockProperties.PostType getRightAttachment();

    void updateAttachments();

    SwingAnimation getSwayingAnimation();
}
