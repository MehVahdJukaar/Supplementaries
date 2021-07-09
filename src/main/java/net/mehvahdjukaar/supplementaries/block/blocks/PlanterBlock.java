package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
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

public class PlanterBlock extends WaterBlock {
    protected static final VoxelShape SHAPE = VoxelShapes.or(VoxelShapes.box(0.125D, 0D, 0.125D, 0.875D, 0.687D, 0.875D), VoxelShapes.box(0D, 0.687D, 0D, 1D, 1D, 1D));
    protected static final VoxelShape SHAPE_C = VoxelShapes.or(VoxelShapes.box(0, 0, 0, 1, 0.9375, 1));

    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED; // raised dirt?

    public PlanterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(EXTENDED, false));
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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(EXTENDED,WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).setValue(EXTENDED, this.canConnect(context.getLevel(),context.getClickedPos()));
    }

    //called when a neighbor is placed
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        if(facing==Direction.UP){
            return stateIn.setValue(EXTENDED, this.canConnect(worldIn, currentPos));
        }
        return stateIn;
    }

    public boolean canConnect(IWorld world, BlockPos pos){
        BlockPos up = pos.above();
        BlockState state = world.getBlockState(up);
        Block b = state.getBlock();
        VoxelShape shape = state.getShape(world, up);
        boolean connect = (!shape.isEmpty() && shape.bounds().minY<0.06);
        return (connect && !(b instanceof StemBlock) && !(b instanceof CropsBlock));
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