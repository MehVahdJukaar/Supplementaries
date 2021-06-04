package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.FlowerBoxBlockTile;
import net.mehvahdjukaar.supplementaries.block.tiles.ItemDisplayTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class FlowerBoxBlock extends WaterBlock{

    protected static final VoxelShape SHAPE_SOUTH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 6.0D);
    protected static final VoxelShape SHAPE_NORTH = Block.box(0.0D, 0.0D, 10.0D, 16.0D, 6.0D, 16.0D);

    protected static final VoxelShape SHAPE_EAST = Block.box(0.0D, 0.0D, 0.0D, 6.0D, 6.0D, 16.0D);
    protected static final VoxelShape SHAPE_WEST = Block.box(10.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);


    protected static final VoxelShape SHAPE_NORTH_FLOOR = Block.box(0.0D, 0.0D, 5.0D, 16.0D, 6.0D, 11.0D);

    protected static final VoxelShape SHAPE_WEST_FLOOR = Block.box(5.0D, 0.0D, 0.0D, 11.0D, 6.0D, 16.0D);


    public static final BooleanProperty FLOOR = BlockProperties.FLOOR;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FlowerBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED,false).setValue(FLOOR,false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, FLOOR);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag).setValue(FLOOR,context.getClickedFace()==Direction.UP)
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof FlowerBoxBlockTile) {
            int ind;

            Direction dir = state.getValue(FACING);
            if(dir.getAxis() == Direction.Axis.X){
                ind = (int)((hit.getLocation().z%1d)/(1/3d));
                if(ind<0)ind = 3+ind;
                ind = MathHelper.clamp(ind,0,2);
                if(dir.getStepX()<0 ) ind = 2-ind;
            }
            else{
                ind = (int)((hit.getLocation().x%1d)/(1/3d));
                if(ind<0)ind = 3+ind;
                ind = MathHelper.clamp(ind,0,2);
                if(dir.getStepZ()>0 ^ dir.getStepX()>0) ind = 2-ind;
            }
            return ((ItemDisplayTile) tileentity).interact(player,handIn,ind);
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FlowerBoxBlockTile();
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof FlowerBoxBlockTile) {
                InventoryHelper.dropContents(world, pos, (IInventory) tileentity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        boolean wall = !state.getValue(FLOOR);
        switch (state.getValue(FACING)) {
            case NORTH :
            default :
                return wall ? SHAPE_NORTH : SHAPE_NORTH_FLOOR;
            case SOUTH :
                return wall ? SHAPE_SOUTH : SHAPE_NORTH_FLOOR;
            case EAST :
                return wall ? SHAPE_EAST : SHAPE_WEST_FLOOR;
            case WEST :
                return wall ? SHAPE_WEST : SHAPE_WEST_FLOOR;
        }
    }

}