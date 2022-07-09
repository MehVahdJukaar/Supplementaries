package net.mehvahdjukaar.supplementaries.common.block.blocks;


import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties.PostType;
import net.mehvahdjukaar.supplementaries.common.block.tiles.RopeKnotBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.quark.QuarkPlugin;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RopeKnotBlock extends MimicBlock implements SimpleWaterloggedBlock, EntityBlock {

    private final Map<BlockState, VoxelShape> SHAPES_MAP = new HashMap<>();
    private final Map<BlockState, VoxelShape> COLLISION_SHAPES_MAP = new HashMap<>();

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final EnumProperty<PostType> POST_TYPE = BlockProperties.POST_TYPE;

    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;


    protected static final Map<Direction, BooleanProperty> FENCE_PROPERTY = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((d) -> d.getKey().getAxis().isHorizontal()).collect(Util.toMap());
    protected static final Map<Direction, EnumProperty<WallSide>> WALL_PROPERTY = ImmutableMap.of(Direction.NORTH, WallBlock.NORTH_WALL, Direction.SOUTH, WallBlock.SOUTH_WALL, Direction.WEST, WallBlock.WEST_WALL, Direction.EAST, WallBlock.EAST_WALL);

    public RopeKnotBlock(Properties properties) {
        super(properties);
        this.makeShapes();

        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y)
                .setValue(WATERLOGGED, false).setValue(POST_TYPE, PostType.POST)
                .setValue(NORTH, false).setValue(SOUTH, false).setValue(WEST, false)
                .setValue(EAST, false).setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, POST_TYPE, AXIS, NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RopeKnotBlockTile(pPos, pState);
    }

    /*
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES_MAP.getOrDefault(state.setValue(WATERLOGGED, false), VoxelShapes.block());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return COLLISION_SHAPES_MAP.getOrDefault(state.setValue(WATERLOGGED, false), VoxelShapes.block());
    }*/


    //this is madness

    /*
    @Override
    public VoxelShape getVisualShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return SHAPES_MAP.getOrDefault(state.setValue(WATERLOGGED, false), VoxelShapes.block());
    }*/


    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            return tile.getShape();
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return SHAPES_MAP.getOrDefault(state.setValue(WATERLOGGED, false), Shapes.block());
    }


    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return SHAPES_MAP.getOrDefault(state.setValue(WATERLOGGED, false), Shapes.block());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            return tile.getCollisionShape();
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    protected void makeShapes() {
        VoxelShape down = Block.box(6, 0, 6, 10, 13, 10);
        VoxelShape up = Block.box(6, 9, 6, 10, 16, 10);
        VoxelShape north = Block.box(6, 9, 0, 10, 13, 10);
        VoxelShape south = Block.box(6, 9, 6, 10, 13, 16);
        VoxelShape west = Block.box(0, 9, 6, 10, 13, 10);
        VoxelShape east = Block.box(6, 9, 6, 16, 13, 10);
        //VoxelShape knot = Block.box(6, 9, 6, 10, 13, 10);

        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            if (state.getValue(WATERLOGGED)) continue;

            VoxelShape v;
            VoxelShape c;
            int w = state.getValue(POST_TYPE).getWidth();
            int o = (16 - w) / 2;
            switch (state.getValue(AXIS)) {
                default -> {
                    v = Block.box(o, 0D, o, o + w, 16D, o + w);
                    c = Block.box(o, 0D, o, o + w, 24, o + w);
                }
                case X -> {
                    v = Block.box(0D, o, o, 16D, o + w, o + w);
                    c = v;
                }
                case Z -> {
                    v = Block.box(o, o, 0, o + w, o + w, 16);
                    c = v;
                }
            }
            if (state.getValue(DOWN)) v = Shapes.or(v, down);
            if (state.getValue(UP)) v = Shapes.or(v, up);
            if (state.getValue(NORTH)) v = Shapes.or(v, north);
            if (state.getValue(SOUTH)) v = Shapes.or(v, south);
            if (state.getValue(WEST)) v = Shapes.or(v, west);
            if (state.getValue(EAST)) v = Shapes.or(v, east);
            c = Shapes.or(c, v);
            c = c.optimize();
            v = v.optimize();
            boolean flag = true;
            for (VoxelShape existing : this.SHAPES_MAP.values()) {
                if (existing.equals(v)) {
                    this.SHAPES_MAP.put(state, existing);
                    flag = false;
                    break;
                }
            }
            if (flag) this.SHAPES_MAP.put(state, v);

            boolean flag2 = true;
            for (VoxelShape existing : this.COLLISION_SHAPES_MAP.values()) {
                if (existing.equals(c)) {
                    this.COLLISION_SHAPES_MAP.put(state, existing);
                    flag2 = false;
                    break;
                }
            }
            if (flag2) this.COLLISION_SHAPES_MAP.put(state, c);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos,
                                  BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            world.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        BlockState newState = state.setValue(RopeBlock.FACING_TO_PROPERTY_MAP.get(facing), RopeBlock.shouldConnectToFace(state, facingState, facingPos, facing, world));
        if (world.getBlockEntity(currentPos) instanceof RopeKnotBlockTile tile) {
            BlockState oldHeld = tile.getHeldBlock();

            RopeKnotBlockTile otherTile = null;
            if (facingState.is(ModRegistry.ROPE_KNOT.get())) {
                if (world.getBlockEntity(facingPos) instanceof RopeKnotBlockTile te2) {
                    otherTile = te2;
                    facingState = otherTile.getHeldBlock();
                }
            }

            BlockState newHeld = null;

            if (CompatHandler.quark) {
                newHeld = QuarkPlugin.updateWoodPostShape(oldHeld, facing, facingState);
            }
            if (newHeld == null) {
                newHeld = oldHeld.updateShape(facing, facingState, world, currentPos, facingPos);
            }

            //manually refreshTextures facing states
            //world.setBlock(currentPos,newHeld,2);
            BlockState newFacing = facingState.updateShape(facing.getOpposite(), newHeld, world, facingPos, currentPos);

            if (newFacing != facingState) {
                if (otherTile != null) {
                    otherTile.setHeldBlock(newFacing);
                    otherTile.setChanged();
                } else {
                    world.setBlock(facingPos, newFacing, 2);
                }
            }

            //BlockState newState = Block.updateFromNeighbourShapes(state, world, toPos);
            // world.setBlockAndUpdate(toPos, newState);

            PostType type = PostType.get(newHeld);

            if (newHeld != oldHeld) {
                tile.setHeldBlock(newHeld);
                tile.setChanged();
            }
            if (newState != state) {
                tile.recalculateShapes(newState);
            }
            if (type != null) {
                newState = newState.setValue(POST_TYPE, type);
            }
        }

        return newState;
    }

    //TODO: fix this not updating mimic block
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        if (rotation == Rotation.CLOCKWISE_180) {
            return state;
        } else {
            return switch (state.getValue(AXIS)) {
                case X -> state.setValue(AXIS, Direction.Axis.Z);
                case Z -> state.setValue(AXIS, Direction.Axis.X);
                default -> state;
            };
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = fluidstate.is(FluidTags.WATER) && fluidstate.getAmount() == 8;
        return this.defaultBlockState().setValue(WATERLOGGED, flag);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_225533_6_) {
        if (player.getItemInHand(hand).getItem() instanceof ShearsItem) {
            if (!world.isClientSide) {
                if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
                    popResource(world, pos, new ItemStack(ModRegistry.ROPE.get()));
                    world.playSound(null, pos, SoundEvents.SNOW_GOLEM_SHEAR, SoundSource.PLAYERS, 0.8F, 1.3F);
                    world.setBlock(pos, tile.getHeldBlock(), 3);
                }
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }


    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            BlockState mimic = tile.getHeldBlock();
            return mimic.getBlock().getCloneItemStack(level, pos, state);
        }
        return super.getCloneItemStack(level, pos, state);
    }


    public static @Nullable BlockState convertToRopeKnot(PostType type, BlockState state, Level world, BlockPos pos) {
        Direction.Axis axis = Direction.Axis.Y;
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            axis = state.getValue(BlockStateProperties.AXIS);
        }
        BlockState newState = ModRegistry.ROPE_KNOT.get().defaultBlockState()
                .setValue(AXIS, axis).setValue(POST_TYPE, type);
        newState = Block.updateFromNeighbourShapes(newState, world, pos);


        if (!world.setBlock(pos, newState, 0)) {
            return null;
        }

        if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            tile.setHeldBlock(state);
            tile.setChanged();
        }
        newState.updateNeighbourShapes(world, pos, UPDATE_CLIENTS | Block.UPDATE_INVISIBLE);
        return newState;
    }
}
