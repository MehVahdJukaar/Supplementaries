package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FodderBlock extends WaterBlock {
    private static final int MAX_LAYERS = 8;
    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[MAX_LAYERS];

    static {
        Arrays.setAll(SHAPE_BY_LAYER, l -> Block.box(0.0D, 0.0D, 0.0D, 16.0D, l * 2D + 2D, 16.0D));
    }

    public FodderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, 8).setValue(WATERLOGGED, false));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter blockGetter, BlockPos pos, PathComputationType pathType) {
        if (pathType == PathComputationType.LAND) {
            return state.getValue(LAYERS) <= MAX_LAYERS / 2;
        }
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return SHAPE_BY_LAYER[state.getValue(LAYERS) - 1];
    }


    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    //ugly but works
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos otherPos) {
        if (facingState.is(this)) {
            if (direction == Direction.UP) {
                int layers = state.getValue(LAYERS);
                int missing = MAX_LAYERS - layers;
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
                int missing = MAX_LAYERS - layers;
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
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this)) {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Math.min(MAX_LAYERS, i + 1));
        } else {
            return super.getStateForPlacement(context);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAYERS);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof HoeItem) {
            world.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!world.isClientSide) {

                int layers = state.getValue(FodderBlock.LAYERS);
                if (layers > 1) {
                    world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                    world.setBlock(pos, state.setValue(FodderBlock.LAYERS, layers - 1), 11);
                } else {
                    world.destroyBlock(pos, false);
                }
                stack.hurtAndBreak(1, player, (e) -> {
                    e.broadcastBreakEvent(hand);
                });
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
