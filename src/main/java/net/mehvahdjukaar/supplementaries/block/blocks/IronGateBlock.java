package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class IronGateBlock extends FenceGateBlock {
    private final boolean gold;
    public IronGateBlock(Properties properties, boolean gold) {
        super(properties);
        this.gold = gold;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        boolean flag = world.hasNeighborSignal(blockpos);
        Direction direction = context.getHorizontalDirection();

        return this.defaultBlockState().setValue(FACING, direction).setValue(OPEN, flag)
                .setValue(POWERED, flag).setValue(IN_WALL, canConnect(world,blockpos,direction));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_, CollisionContext p_220053_4_) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    //better done here cause of side + up
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        if (!world.isClientSide) {
            boolean flag = world.hasNeighborSignal(pos);
            if (state.getValue(POWERED) != flag) {
                state = state.setValue(POWERED, flag);
                if(!gold || !ServerConfigs.cached.CONSISTENT_GATE){
                    if (state.getValue(OPEN) != flag) {
                        world.levelEvent(null, flag ? 1036 : 1037, pos, 0);
                    }
                    state = state.setValue(OPEN, flag);
                }
            }
            boolean connect = canConnect(world,pos,state.getValue(FACING));
            world.setBlock(pos,state.setValue(IN_WALL,connect),2);
        }
    }




    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState other, LevelAccessor world, BlockPos pos, BlockPos otherPos) {
        return state;
    }

    private boolean canConnect(LevelAccessor world, BlockPos pos, Direction dir){
        return canConnectUp(world.getBlockState(pos.above()),world,pos.above()) ||
                canConnectSide(world.getBlockState(pos.relative(dir.getClockWise()))) ||
                canConnectSide(world.getBlockState(pos.relative(dir.getCounterClockWise())));
    }

    private boolean canConnectSide(BlockState state){
        return state.getBlock() instanceof IronBarsBlock;
    }

    private boolean canConnectUp(BlockState state, LevelAccessor world, BlockPos pos){
        return state.isFaceSturdy(world,pos,Direction.DOWN) || state.is(this) || state.getBlock() instanceof IronBarsBlock;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {

        if(!state.getValue(POWERED) && gold || !ServerConfigs.cached.CONSISTENT_GATE){
            Direction dir = player.getDirection();


            if(ServerConfigs.cached.DOUBLE_IRON_GATE){
                BlockPos up = pos.above();
                BlockState stateUp = world.getBlockState(up);
                if(stateUp.is(this) && stateUp.setValue(IN_WALL,false) == state.setValue(IN_WALL,false))
                    openGate(stateUp,world,up,dir);
                BlockPos down = pos.below();
                BlockState stateDown = world.getBlockState(down);
                if(stateDown.is(this) && stateDown.setValue(IN_WALL,false) == state.setValue(IN_WALL,false))
                    openGate(stateDown,world,down,dir);
            }

            openGate(state,world,pos,dir);

            world.levelEvent(player, state.getValue(OPEN) ? 1036 : 1037, pos, 0);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;

    }

    private void openGate(BlockState state, Level world, BlockPos pos, Direction dir){
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
