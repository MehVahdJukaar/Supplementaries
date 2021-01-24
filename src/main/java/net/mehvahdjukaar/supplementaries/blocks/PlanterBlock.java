package net.mehvahdjukaar.supplementaries.blocks;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.IPlantable;


public class PlanterBlock extends Block implements IWaterLoggable{
    protected static final VoxelShape SHAPE = VoxelShapes.or(VoxelShapes.create(0.125D, 0D, 0.125D, 0.875D, 0.687D, 0.875D), VoxelShapes.create(0D, 0.687D, 0D, 1D, 1D, 1D));
    protected static final VoxelShape SHAPE_C = VoxelShapes.or(VoxelShapes.create(0, 0, 0, 1, 0.9375, 1));

    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED; // raised dirt?
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PlanterBlock(Properties properties) {
        super(properties);

        this.setDefaultState(this.stateContainer.getBaseState().with(WATERLOGGED, false).with(EXTENDED, false));
    }
    //TODO: this seem sto fix pathfinding
    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE_C;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(EXTENDED,WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        return this.updatedState(this.getDefaultState(), context.getWorld(), context.getPos()).with(WATERLOGGED, flag);
    }

    //called when a neighbor is placed
    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        if(facing==Direction.UP){
            return this.updatedState(stateIn, worldIn, currentPos);
        }
        return stateIn;
    }

    public BlockState updatedState(BlockState state, IWorld world, BlockPos pos){
        return state.with(EXTENDED, this.canConnect(world.getBlockState(pos.up()).getBlock()));
    }

    public boolean canConnect(Block block){
        return !((block instanceof AirBlock) || (block instanceof StemBlock) || (block instanceof CropsBlock));
    }

    @Override
    public boolean isFertile(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction direction, IPlantable plantable) {
        return true;
    }




}