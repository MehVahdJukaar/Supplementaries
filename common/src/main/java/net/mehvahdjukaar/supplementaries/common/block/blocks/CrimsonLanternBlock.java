package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CrimsonLanternBlock extends LanternBlock {

    public static BooleanProperty CONNECTED = BlockStateProperties.ATTACHED;

    private static final VoxelShape SHAPE = Shapes.or(Block.box(4.0D, 1.0D, 4.0D, 12.0D, 8.0D, 12.0D),
            Block.box(6.0D, 0.0D, 6.0D, 10.0D, 9.0D, 10.0D));
    private static final VoxelShape SHAPE_UP = Shapes.or(Block.box(4.0D, 6.0D, 4.0D, 12.0D, 13.0D, 12.0D),
            Block.box(6.0D, 5.0D, 6.0D, 10.0D, 14.0D, 10.0D));

    public CrimsonLanternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(CONNECTED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? SHAPE_UP : SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONNECTED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            state = state.setValue(CONNECTED, neighborState.is(this) && neighborState.getValue(HANGING));
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if(level.getBlockState(pos.above()).is(this) )return true;
        return super.canSurvive(state, level, pos);
    }
}
