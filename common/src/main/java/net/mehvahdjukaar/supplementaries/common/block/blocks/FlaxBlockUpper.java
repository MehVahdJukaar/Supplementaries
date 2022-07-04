package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlaxBlockUpper extends Block{
    private static final VoxelShape[] SHAPES_TOP = new VoxelShape[]{
            Block.box(2, 0, 2, 14, 3, 14),
            Block.box(1, 0, 1, 15, 7, 15),
            Block.box(1, 0, 1, 15, 11, 15),
            Block.box(1, 0, 1, 15, 16, 15),};
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public FlaxBlockUpper(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPES_TOP[state.getValue(AGE)];
    }

    //double plant code
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {

        if(facing == Direction.DOWN && isValidLowerStage(facingState)){
            int ageBelow = facingState.getValue(FlaxBlock.AGE);
            if(ageBelow>=FlaxBlock.DOUBLE_AGE){
                int targetAge = ageBelow - FlaxBlock.DOUBLE_AGE;
                if(stateIn.getValue(AGE) != targetAge){
                    //follow lower stage growth
                    return stateIn.setValue(AGE, targetAge);
                }
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return isValidLowerStage(worldIn.getBlockState(pos.below()));
    }

    public boolean isValidLowerStage(BlockState state){
        return state.getBlock() instanceof FlaxBlock;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(AGE);
    }
}
