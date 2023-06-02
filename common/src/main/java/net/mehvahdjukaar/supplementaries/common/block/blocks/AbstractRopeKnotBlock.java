package net.mehvahdjukaar.supplementaries.common.block.blocks;


import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.supplementaries.common.block.IRopeConnection;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties.PostType;
import net.mehvahdjukaar.supplementaries.common.block.tiles.RopeKnotBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractRopeKnotBlock extends MimicBlock implements SimpleWaterloggedBlock, EntityBlock, IRotatable {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final EnumProperty<PostType> POST_TYPE = ModBlockProperties.POST_TYPE;

    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;


    private final Map<BlockState, VoxelShape> shapeMap;


    protected AbstractRopeKnotBlock(Properties properties) {
        super(properties);
        var s = this.makeShapes();
        shapeMap = s.getFirst();

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
            try {
                return tile.getShape();
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to get block shape for rope knot block at {}", pos);
            }
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return shapeMap.getOrDefault(state.setValue(WATERLOGGED, false), Shapes.block());
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return shapeMap.getOrDefault(state.setValue(WATERLOGGED, false), Shapes.block());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            try {
                return tile.getCollisionShape();
            } catch (Exception e) {
                Supplementaries.LOGGER.error("Failed to get collision shape for rope knot block at {}", pos);
            }
        }
        return super.getCollisionShape(state, world, pos, context);
    }

    protected Pair<Map<BlockState, VoxelShape>, Map<BlockState, VoxelShape>> makeShapes() {
        Map<BlockState, VoxelShape> shapesBuilder = new HashMap<>();
        Map<BlockState, VoxelShape> collisionBuilder = new HashMap<>();


        VoxelShape down = Block.box(6, 0, 6, 10, 13, 10);
        VoxelShape up = Block.box(6, 9, 6, 10, 16, 10);
        VoxelShape north = getSideShape();
        VoxelShape south = MthUtils.rotateVoxelShape(north, Direction.SOUTH);
        VoxelShape west = MthUtils.rotateVoxelShape(north, Direction.WEST);
        VoxelShape east = MthUtils.rotateVoxelShape(north, Direction.EAST);
        //VoxelShape knot = Block.box(6, 9, 6, 10, 13, 10);

        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            if (state.getValue(WATERLOGGED)) continue;

            VoxelShape v;
            VoxelShape c;
            int w = state.getValue(POST_TYPE).getWidth();
            double o = (16 - w) / 2d;
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
            for (VoxelShape existing : shapesBuilder.values()) {
                if (existing.equals(v)) {
                    shapesBuilder.put(state, existing);
                    flag = false;
                    break;
                }
            }
            if (flag) shapesBuilder.put(state, v);

            boolean flag2 = true;
            for (VoxelShape existing : collisionBuilder.values()) {
                if (existing.equals(c)) {
                    collisionBuilder.put(state, existing);
                    flag2 = false;
                    break;
                }
            }
            if (flag2) collisionBuilder.put(state, c);
        }
        return Pair.of(ImmutableMap.copyOf(shapesBuilder), ImmutableMap.copyOf(collisionBuilder));
    }

    public abstract VoxelShape getSideShape();

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        state = switch (rotation) {
            case CLOCKWISE_180 ->
                    state.setValue(NORTH, state.getValue(SOUTH)).setValue(EAST, state.getValue(WEST)).setValue(SOUTH, state.getValue(NORTH)).setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90 ->
                    state.setValue(NORTH, state.getValue(EAST)).setValue(EAST, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(WEST)).setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90 ->
                    state.setValue(NORTH, state.getValue(WEST)).setValue(EAST, state.getValue(NORTH)).setValue(SOUTH, state.getValue(EAST)).setValue(WEST, state.getValue(SOUTH));
            default -> state;
        };
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
    public BlockState mirror(BlockState state, Mirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK -> state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation, Direction axis, @Nullable Vec3 hit) {
        if(axis.getAxis() == Direction.Axis.Y){
            return Optional.ofNullable(this.rotate(state, rotation));
        }
        return Optional.empty();
    }

    @Override
    public void onRotated(BlockState newState, BlockState oldState, LevelAccessor world, BlockPos pos, Rotation rotation, Direction axis, @Nullable Vec3 hit) {
        if (axis.getAxis() == Direction.Axis.Y && world.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            BlockState mimic = tile.getHeldBlock();
            BlockState newMimic = tile.getHeldBlock().rotate(rotation);
            if (newMimic != mimic) {
                tile.setHeldBlock(newMimic);
                tile.setChanged();
            }
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
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof RopeKnotBlockTile tile) {
            BlockState mimic = tile.getHeldBlock();
            return mimic.getBlock().getCloneItemStack(level, pos, state);
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Nullable
    public static BlockState convertToRopeKnot(PostType type, BlockState state, Level world, BlockPos pos) {
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
