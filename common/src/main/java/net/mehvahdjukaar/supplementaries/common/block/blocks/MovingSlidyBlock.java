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

public class MovingSlidyBlock extends MovingPistonBlock {
    public MovingSlidyBlock(Properties properties) {
        super(properties);
    }

    public static BlockEntity newMovingBlockEntity(BlockPos pos, BlockState blockState, BlockState movedState, Direction direction, boolean extending, boolean isSourcePiston) {
        return new MovingSlidyBlockEntity(pos, blockState, movedState, direction, extending, isSourcePiston);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModRegistry.MOVING_SLIDY_BLOCK_TILE.get(), MovingSlidyBlockEntity::tick);
    }

    public static void move(BlockState state, Level level, BlockPos pos, Direction direction, BlockPos neighborPos) {
        BlockState newState = ModRegistry.MOVING_SLIDY_BLOCK.get().defaultBlockState()
                .setValue(MovingSlidyBlock.FACING, direction);
        level.setBlock(neighborPos, newState, 3);
        level.setBlockEntity(MovingSlidyBlock.newMovingBlockEntity(neighborPos, newState, state,
                direction, true, false));
        level.setBlock(pos, ModRegistry.MOVING_SLIDY_BLOCK_SOURCE.get()
                .defaultBlockState().setValue(BlockStateProperties.FACING, direction), Block.UPDATE_NONE);
    }

}
