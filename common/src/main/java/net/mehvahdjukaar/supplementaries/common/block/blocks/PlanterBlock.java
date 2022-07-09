package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;

public class PlanterBlock extends WaterBlock {
    protected static final VoxelShape SHAPE = Shapes.or(Shapes.box(0.125D, 0D, 0.125D, 0.875D, 0.687D, 0.875D), Shapes.box(0D, 0.687D, 0D, 1D, 1D, 1D));
    protected static final VoxelShape SHAPE_C = Shapes.or(Shapes.box(0, 0, 0, 1, 0.9375, 1));

    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED; // raised dirt?

    public PlanterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(EXTENDED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE_C;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EXTENDED, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(EXTENDED, this.canConnect(context.getLevel(), context.getClickedPos()));
    }

    //called when a neighbor is placed
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        if (facing == Direction.UP) {
            return stateIn.setValue(EXTENDED, this.canConnect(worldIn, currentPos));
        }
        return stateIn;
    }

    public boolean canConnect(LevelAccessor world, BlockPos pos) {
        BlockPos up = pos.above();
        BlockState state = world.getBlockState(up);
        Block b = state.getBlock();
        VoxelShape shape = state.getShape(world, up);
        boolean connect = (!shape.isEmpty() && shape.bounds().minY < 0.06);
        return (connect && !(b instanceof StemBlock) && !(b instanceof CropBlock));
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction direction, IPlantable plantable) {
        return true;
    }
}