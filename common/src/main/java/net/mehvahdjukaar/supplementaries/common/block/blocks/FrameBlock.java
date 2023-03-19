package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FrameBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameBlock extends MimicBlock implements EntityBlock, IFrameBlock {

    public static final List<Block> FRAMED_BLOCKS = new ArrayList<>();

    public static final BooleanProperty HAS_BLOCK = ModBlockProperties.HAS_BLOCK;
    public static final IntegerProperty LIGHT_LEVEL = ModBlockProperties.LIGHT_LEVEL_0_15;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final VoxelShape OCCLUSION_SHAPE = Block.box(0.01, 0.01, 0.01, 15.99, 15.99, 15.99);

    private final Map<Block, Block> filledBlocks = new HashMap<>();

    public FrameBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIGHT_LEVEL)));
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false)
                .setValue(LIGHT_LEVEL, 0).setValue(HAS_BLOCK, false));
        FRAMED_BLOCKS.add(this);
    }

    public void registerFilledBlock(Block inserted, Block filled) {
        filledBlocks.put(inserted, filled);
    }

    @Override
    public @Nullable Block getFilledBlock(Block inserted) {
        return filledBlocks.get(inserted);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        if (adjacentBlockState.getBlock() instanceof FrameBlock) {
            boolean hasBlock = state.getValue(HAS_BLOCK);
            boolean neighborHasBlock = adjacentBlockState.getValue(HAS_BLOCK);
            return hasBlock == neighborHasBlock || super.skipRendering(state, adjacentBlockState, side);
        }
        return super.skipRendering(state, adjacentBlockState, side);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FrameBlockTile(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL, HAS_BLOCK, WATERLOGGED);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        if (world.getBlockEntity(pos) instanceof FrameBlockTile tile) {
            return tile.handleInteraction(world, pos, player, hand, trace, true);
        }
        return InteractionResult.PASS;
    }

    //handles dynamic culling
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos) {
        if (state.getValue(HAS_BLOCK)) {
            if (reader.getBlockEntity(pos) instanceof FrameBlockTile tile && !tile.getHeldBlock().isAir()) {
                return Shapes.block();
            }
        }
        return OCCLUSION_SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        if (state.getValue(HAS_BLOCK)) {
            return Shapes.block();
        }
        return super.getCollisionShape(state, reader, pos, context); //return OCCLUSION_SHAPE
    }

    //occlusion shading
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter reader, BlockPos pos) {
        return state.getValue(HAS_BLOCK) ? 0.2f : 1;
    }

    //let light through
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return !state.getValue(HAS_BLOCK) || super.propagatesSkylightDown(state, reader, pos);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof FrameBlockTile tile) {
            tile.getHeldBlock().getAnalogOutputSignal(world, pos);
        }
        return 0;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return stateIn;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        if (state.getValue(HAS_BLOCK)) return false;
        return switch (type) {
            case LAND, AIR -> true;
            case WATER -> worldIn.getFluidState(pos).is(FluidTags.WATER);
        };
    }
}
