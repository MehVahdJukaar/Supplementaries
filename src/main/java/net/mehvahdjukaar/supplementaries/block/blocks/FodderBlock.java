package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;
import java.util.Arrays;

public class FodderBlock extends WaterBlock {

    public static final IntegerProperty LAYERS = BlockProperties.LAYERS;
    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[16];

    static {
        Arrays.setAll(SHAPE_BY_LAYER, l -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, l+1, 16.0D));
    }

    public FodderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 16).setValue(WATERLOGGED,false));
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType pathType) {
        if (pathType == PathType.LAND) {
            return state.getValue(LAYERS) < 8;
        }
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS) - 1];
    }


    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }


    @Override
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState blockstate = world.getBlockState(below);
        return Block.isFaceFull(blockstate.getCollisionShape(world, below), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState oldState, IWorld world, BlockPos p_196271_5_, BlockPos p_196271_6_) {
        return !state.canSurvive(world, p_196271_5_) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, oldState, world, p_196271_5_, p_196271_6_);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
        int i = state.getValue(LAYERS);
        if (context.getItemInHand().getItem() == this.asItem() && i < 16) {
            if (context.replacingClickedOnBlock()) {
                return context.getClickedFace() == Direction.UP;
            } else {
                return true;
            }
        } else {
            return i == 1;
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this)) {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Math.min(16, i + 1));
        } else {
            return super.getStateForPlacement(context);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAYERS);
    }
}
