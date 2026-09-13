package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.candlelight.api.VirtualOverride;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.GrouperMatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class GrouperBlock extends HorizontalDirectionalBlock {

    public static final EnumProperty<GrouperMatch> MATCH = ModBlockProperties.GROUPER_MATCH;
    private static final MapCodec<GrouperBlock> CODEC = simpleCodec(GrouperBlock::new);

    public GrouperBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(MATCH, GrouperMatch.NONE));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MATCH);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, facing)
                .setValue(MATCH, findMatch(context.getLevel(), context.getClickedPos(), facing));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (state.getValue(MATCH) != GrouperMatch.NONE) {
            this.updateNeighborsInFront(level, pos, state);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        GrouperMatch match = findMatch(level, pos, state.getValue(FACING));
        if (match == state.getValue(MATCH)) return;
        level.setBlock(pos, state.setValue(MATCH, match), 3);
        this.updateNeighborsInFront(level, pos, state);
    }

    //strong power has to go through whatever is in front, so that block's own neighbors need poking too
    private void updateNeighborsInFront(Level level, BlockPos pos, BlockState state) {
        level.updateNeighborsAt(pos.relative(state.getValue(FACING)), this);
    }

    private GrouperMatch findMatch(BlockGetter level, BlockPos pos, Direction facing) {
        BlockState left = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        BlockState right = level.getBlockState(pos.relative(facing.getClockWise()));
        if (left.isAir() || !left.is(right.getBlock())) return GrouperMatch.NONE;
        if (left.is(this)) {
            return left.getValue(FACING) == right.getValue(FACING) ? GrouperMatch.FULL : GrouperMatch.PARTIAL;
        }
        return left == right ? GrouperMatch.FULL : GrouperMatch.PARTIAL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock()) && state.getValue(MATCH) != GrouperMatch.NONE) {
            this.updateNeighborsInFront(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return side == state.getValue(FACING).getOpposite() ? state.getValue(MATCH).power : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return this.getSignal(state, level, pos, side);
    }

    @VirtualOverride("neoforge")
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return direction == state.getValue(FACING).getOpposite();
    }
}
