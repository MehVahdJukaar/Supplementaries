package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.tiles.MovingSlidyBlockEntity;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.PushReaction;

public class MovingSlidyBlock extends MovingPistonBlock {

    public MovingSlidyBlock(Properties properties) {
        super(properties);
    }

    public static BlockEntity newMovingBlockEntity(BlockPos pos, BlockState blockState, BlockState movedState, Direction direction) {
        return new MovingSlidyBlockEntity(pos, blockState, movedState, direction, true, false);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModRegistry.MOVING_SLIDY_BLOCK_TILE.get(), MovingSlidyBlockEntity::tick);
    }

    public static boolean maybeMove(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighbor = level.getBlockState(neighborPos);
        if (!neighbor.isAir() && neighbor.getPistonPushReaction() != PushReaction.DESTROY) {
            return false;
        }
        level.destroyBlock(neighborPos, true);

        MovingSlidyBlock.move(state, level, pos, direction, neighborPos);

        return true;
    }

    public static void move(BlockState state, Level level, BlockPos pos, Direction direction, BlockPos neighborPos) {
        BlockState newState = ModRegistry.MOVING_SLIDY_BLOCK.get().defaultBlockState()
                .setValue(MovingSlidyBlock.FACING, direction);
        level.setBlock(neighborPos, newState, 67);
        BlockEntity be = MovingSlidyBlock.newMovingBlockEntity(neighborPos, newState, state, direction);
        level.setBlockEntity(be);

        level.setBlock(pos, ModRegistry.MOVING_SLIDY_BLOCK_SOURCE.get()
                .defaultBlockState().setValue(BlockStateProperties.FACING, direction), Block.UPDATE_ALL);
    }

}
