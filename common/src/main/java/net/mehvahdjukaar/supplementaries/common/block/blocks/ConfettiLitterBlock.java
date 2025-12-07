package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.BiFunction;

public class ConfettiLitterBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ConfettiLitterBlock> CODEC = simpleCodec(ConfettiLitterBlock::new);

    public static final IntegerProperty AMOUNT = ModBlockProperties.CONFETTI_AMOUNT;
    private static final BiFunction<Direction, Integer, VoxelShape> SHAPE_BY_PROPERTIES = Util.memoize((direction, integer) -> {
        VoxelShape[] voxelShapes = new VoxelShape[]{Block.box(8.0, 0.0, 8.0, 16.0, 1.0, 16.0), Block.box(8.0, 0.0, 0.0, 16.0, 1.0, 8.0), Block.box(0.0, 0.0, 0.0, 8.0, 1.0, 8.0), Block.box(0.0, 0.0, 8.0, 8.0, 1.0, 16.0)};
        VoxelShape voxelShape = Shapes.empty();

        for (int i = 0; i < integer; ++i) {
            int j = Math.floorMod(i - direction.get2DDataValue(), 4);
            voxelShape = Shapes.or(voxelShape, voxelShapes[j]);
        }

        return voxelShape.singleEncompassing();
    });

    public ConfettiLitterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        return useContext.getItemInHand().is(this.asItem())
                && state.getValue(AMOUNT) < 4 || super.canBeReplaced(state, useContext);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_PROPERTIES.apply(state.getValue(FACING), state.getValue(AMOUNT));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockState = context.getLevel().getBlockState(context.getClickedPos());
        return blockState.is(this) ? blockState.setValue(AMOUNT, Math.min(4, blockState.getValue(AMOUNT) + 1)) :
                this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return MultifaceBlock.canAttachTo(level, Direction.UP, pos, level.getBlockState(pos.below()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AMOUNT);
    }

}
