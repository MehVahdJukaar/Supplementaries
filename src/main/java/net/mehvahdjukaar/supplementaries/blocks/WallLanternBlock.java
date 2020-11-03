package net.mehvahdjukaar.supplementaries.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;



public class WallLanternBlock extends Block {
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    public WallLanternBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        /*
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof WallLanternBlockTile) {
            return ((WallLanternBlockTile) te).lanternBlock.getLightValue();
        }*/
        //may cause lag
        return 15;
    }


    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof WallLanternBlockTile) {
            return new ItemStack(((WallLanternBlockTile) te).lanternBlock.getBlock());
        }
        return new ItemStack(Blocks.LANTERN, 1);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockpos = pos.offset(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        return blockstate.isSolidSide(worldIn, blockpos, direction);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos,
                                          BlockPos facingPos) {
        return facing == stateIn.get(FACING).getOpposite() && !stateIn.isValidPosition(worldIn, currentPos)
                ? Blocks.AIR.getDefaultState()
                : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof WallLanternBlockTile) {
            ((WallLanternBlockTile) tileentity).counter = 0;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.get(FACING)) {
            case UP :
            case DOWN :
            case SOUTH :
            default :
                return VoxelShapes.create(0.6875D, 0.125D, 0.625D, 0.3125D, 1D, 0D);
            case NORTH :
                return VoxelShapes.create(0.3125D, 0.125D, 0.375D, 0.6875D, 1D, 1D);
            case WEST :
                return VoxelShapes.create(0.375D, 0.125D, 0.6875D, 1D, 1D, 0.3125D);
            case EAST :
                return VoxelShapes.create(0.625D, 0.125D, 0.3125D, 0D, 1D, 0.6875D);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        if (context.getFace() == Direction.UP || context.getFace() == Direction.DOWN)
            return this.getDefaultState().with(FACING, Direction.NORTH);
        return this.getDefaultState().with(FACING, context.getFace());
    }


    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof WallLanternBlockTile) {
            spawnDrops(((WallLanternBlockTile) te).lanternBlock, worldIn, pos);

        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new WallLanternBlockTile();
    }

    @Override
    public boolean eventReceived(BlockState state, World world, BlockPos pos, int eventID, int eventParam) {
        super.eventReceived(state, world, pos, eventID, eventParam);
        TileEntity tileentity = world.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
    }
}