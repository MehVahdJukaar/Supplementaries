package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.ClockBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ClockBlock extends WaterBlock {
    protected static final VoxelShape SHAPE_NORTH = VoxelShapes.box(1D, 0D, 1D, 0D, 1D, 0.0625D);
    protected static final VoxelShape SHAPE_SOUTH = VoxelShapes.box(0D, 0D, 0D, 1D, 1D, 0.9375D);
    protected static final VoxelShape SHAPE_EAST = VoxelShapes.box(0D, 0D, 1D, 0.9375D, 1D, 0D);
    protected static final VoxelShape SHAPE_WEST = VoxelShapes.box(1D, 0D, 0D, 0.0625D, 1D, 1D);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty HOUR = BlockProperties.HOUR;

    public ClockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false).setValue(FACING, Direction.NORTH));
    }


    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return super.getRenderShape(state);
        //return BlockRenderType.MODEL;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        if (!worldIn.isClientSide()) {
            int time = ((int) (worldIn.getDayTime()+6000) % 24000);
            int m = (int) (((time % 1000f) / 1000f) * 60);
            int h = time / 1000;
            String a ="";
            if(!ClientConfigs.cached.CLOCK_24H) {
                a = time < 12000 ? " AM" : " PM";
                h=h%12;
            }
            player.displayClientMessage(new StringTextComponent(h + ":" + ((m<10)?"0":"") + m + a), true);

        }
        return ActionResultType.sidedSuccess(worldIn.isClientSide);
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        switch (state.getValue(FACING)) {
            case NORTH :
            default :
                return SHAPE_NORTH;
            case SOUTH :
                return SHAPE_SOUTH;
            case EAST :
                return SHAPE_EAST;
            case WEST :
                return SHAPE_WEST;
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ClockBlockTile();
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof ClockBlockTile) {
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(HOUR,FACING,WATERLOGGED);
    }

    @Override
    public boolean hasAnalogOutputSignal(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if(te instanceof ClockBlockTile){
            return ((ClockBlockTile) te).power;
        }
        return 0;
    }

    @Override
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, worldIn, pos, oldState, isMoving);
        TileEntity te = worldIn.getBlockEntity(pos);
        if(te instanceof ClockBlockTile){
            ((ClockBlockTile) te).updateInitialTime();
        }
    }


}
