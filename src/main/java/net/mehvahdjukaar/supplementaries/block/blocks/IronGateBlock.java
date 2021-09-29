package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class IronGateBlock extends FenceGateBlock {
    private final boolean gold;
    public IronGateBlock(Properties properties, boolean gold) {
        super(properties);
        this.gold = gold;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        boolean flag = world.hasNeighborSignal(blockpos);
        Direction direction = context.getHorizontalDirection();

        return this.defaultBlockState().setValue(FACING, direction).setValue(OPEN, flag)
                .setValue(POWERED, flag).setValue(IN_WALL, canConnect(world,blockpos,direction));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    //better done here cause of side + up
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
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
    public BlockState updateShape(BlockState state, Direction dir, BlockState other, IWorld world, BlockPos pos, BlockPos otherPos) {
        return state;
    }

    private boolean canConnect(IWorld world, BlockPos pos, Direction dir){
        return canConnectUp(world.getBlockState(pos.above()),world,pos.above()) ||
                canConnectSide(world.getBlockState(pos.relative(dir.getClockWise()))) ||
                canConnectSide(world.getBlockState(pos.relative(dir.getCounterClockWise())));
    }

    private boolean canConnectSide(BlockState state){
        return state.getBlock() instanceof PaneBlock;
    }

    private boolean canConnectUp(BlockState state, IWorld world, BlockPos pos){
        return state.isFaceSturdy(world,pos,Direction.DOWN) || state.is(this) || state.getBlock() instanceof PaneBlock;
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {

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
            return ActionResultType.sidedSuccess(world.isClientSide);
        }

        return ActionResultType.PASS;

    }

    private void openGate(BlockState state, World world, BlockPos pos, Direction dir){
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
