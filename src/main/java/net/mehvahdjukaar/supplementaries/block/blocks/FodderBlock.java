package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;

public class FodderBlock extends WaterBlock {

    public static final IntegerProperty LAYERS = BlockProperties.LAYERS;
    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[16];

    static {
        Arrays.setAll(SHAPE_BY_LAYER, l -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, l + 1, 16.0D));
    }

    public FodderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 16).setValue(WATERLOGGED, false));
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
        if (true) return true;
        BlockPos below = pos.below();
        BlockState blockstate = world.getBlockState(below);
        return Block.isFaceFull(blockstate.getCollisionShape(world, below), Direction.UP);
    }

    //ugly but works
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos otherPos) {
        if (facingState.is(this)) {
            if (direction == Direction.UP) {
                int layers = state.getValue(LAYERS);
                int missing = 16 - layers;
                if (missing > 0) {
                    int otherLayers = facingState.getValue(LAYERS);
                    int newOtherLayers = otherLayers - missing;
                    BlockState newOtherState;
                    if (newOtherLayers <= 0) {
                        newOtherState = Blocks.AIR.defaultBlockState();
                    } else {
                        newOtherState = facingState.setValue(LAYERS, newOtherLayers);
                    }
                    BlockState newState = state.setValue(LAYERS, layers + otherLayers - Math.max(0, newOtherLayers));
                    world.setBlock(currentPos, newState, 0);
                    world.setBlock(otherPos, newOtherState, 0);
                    return newState;
                }
            } else if (direction == Direction.DOWN) {
                int layers = facingState.getValue(LAYERS);
                int missing = 16 - layers;
                if (missing > 0) {
                    int myLayers = state.getValue(LAYERS);
                    int myNewLayers = myLayers - missing;
                    BlockState myNewState;
                    if (myNewLayers <= 0) {
                        myNewState = Blocks.AIR.defaultBlockState();
                    } else {
                        myNewState = state.setValue(LAYERS, myNewLayers);
                    }
                    world.setBlock(otherPos, state.setValue(LAYERS, layers + myLayers - Math.max(0, myNewLayers)), 0);
                    return myNewState;
                }
            }
        }
        return super.updateShape(state, direction, facingState, world, currentPos, otherPos);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockItemUseContext context) {
        if (true) return false;
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

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof HoeItem) {
            world.playSound(player, pos, SoundEvents.HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!world.isClientSide) {

                int layers = state.getValue(FodderBlock.LAYERS);
                if (layers > 2) {
                    world.levelEvent(2001, pos, Block.getId(state));
                    world.setBlock(pos, state.setValue(FodderBlock.LAYERS, layers - 2), 11);
                } else {
                    world.destroyBlock(pos, false);
                }
                stack.hurtAndBreak(1, player, (e) -> {
                    e.broadcastBreakEvent(hand);
                });
            }

            return ActionResultType.sidedSuccess(world.isClientSide);
        }
        return ActionResultType.PASS;
    }
}
