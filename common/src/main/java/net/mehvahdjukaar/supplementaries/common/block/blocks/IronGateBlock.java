package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IronGateBlock extends FenceGateBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final boolean gold;

    public IronGateBlock(Properties properties, boolean gold) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE));
        this.gold = gold;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return stateIn;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, POWERED, IN_WALL, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        boolean flag = world.hasNeighborSignal(blockpos);
        Direction direction = context.getHorizontalDirection();
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        BlockState state = this.defaultBlockState().setValue(WATERLOGGED, fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8);

        return state.setValue(FACING, direction).setValue(OPEN, flag)
                .setValue(POWERED, flag).setValue(IN_WALL, canConnect(world, blockpos, direction));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    //better done here cause of side + up
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        if (!world.isClientSide) {
            boolean flag = world.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != flag) {
                state = state.setValue(POWERED, flag);
                if (!gold || !CommonConfigs.Building.CONSISTENT_GATE.get()) {
                    if (state.getValue(OPEN) != flag) {
                        world.levelEvent(null, flag ? 1036 : 1037, pos, 0);
                        world.gameEvent(null, flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
                    }
                    state = state.setValue(OPEN, flag);
                }
            }
            boolean connect = canConnect(world, pos, state.getValue(FACING));
            world.setBlock(pos, state.setValue(IN_WALL, connect), 2);
        }
    }


    private boolean canConnect(LevelAccessor world, BlockPos pos, Direction dir) {
        return canConnectUp(world.getBlockState(pos.above()), world, pos.above()) ||
                canConnectSide(world.getBlockState(pos.relative(dir.getClockWise()))) ||
                canConnectSide(world.getBlockState(pos.relative(dir.getCounterClockWise())));
    }

    private boolean canConnectSide(BlockState state) {
        return state.getBlock() instanceof IronBarsBlock;
    }

    private boolean canConnectUp(BlockState state, LevelAccessor world, BlockPos pos) {
        return state.isFaceSturdy(world, pos, Direction.DOWN) || state.is(this) || state.getBlock() instanceof IronBarsBlock;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {

        if (!state.getValue(POWERED) && gold || !CommonConfigs.Building.CONSISTENT_GATE.get()) {
            Direction dir = player.getDirection();


            if (CommonConfigs.Building.DOUBLE_IRON_GATE.get()) {
                BlockPos up = pos.above();
                BlockState stateUp = world.getBlockState(up);
                if (stateUp.is(this) && stateUp.setValue(IN_WALL, false) == state.setValue(IN_WALL, false))
                    openGate(stateUp, world, up, dir);
                BlockPos down = pos.below();
                BlockState stateDown = world.getBlockState(down);
                if (stateDown.is(this) && stateDown.setValue(IN_WALL, false) == state.setValue(IN_WALL, false))
                    openGate(stateDown, world, down, dir);
            }

            openGate(state, world, pos, dir);
            boolean open = state.getValue(OPEN);
            world.levelEvent(player, open ? 1036 : 1037, pos, 0);
            world.gameEvent(player, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;

    }

    private void openGate(BlockState state, Level world, BlockPos pos, Direction dir) {
        if (state.getValue(OPEN)) {
            state = state.setValue(OPEN, Boolean.FALSE);
        } else {
            if (state.getValue(FACING) == dir.getOpposite()) {
                state = state.setValue(FACING, dir);
            }
            state = state.setValue(OPEN, Boolean.TRUE);
        }
        world.setBlock(pos, state, 10);
    }

}
