package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BuntingBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BuntingBlock extends AbstractRopeBlock implements EntityBlock, IRotatable {

    public static final EnumProperty<ModBlockProperties.Bunting> NORTH = ModBlockProperties.NORTH_BUNTING;
    public static final EnumProperty<ModBlockProperties.Bunting> SOUTH = ModBlockProperties.SOUTH_BUNTING;
    public static final EnumProperty<ModBlockProperties.Bunting> WEST = ModBlockProperties.WEST_BUNTING;
    public static final EnumProperty<ModBlockProperties.Bunting> EAST = ModBlockProperties.EAST_BUNTING;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    public static final Map<Direction, EnumProperty<ModBlockProperties.Bunting>> HORIZONTAL_FACING_TO_PROPERTY_MAP = Util.make(Maps.newEnumMap(Direction.class), (directions) -> {
        directions.put(Direction.NORTH, NORTH);
        directions.put(Direction.EAST, EAST);
        directions.put(Direction.SOUTH, SOUTH);
        directions.put(Direction.WEST, WEST);
    });

    public final Map<BlockState, BlockState> buntingToRope = new Object2ObjectOpenHashMap<>();

    public BuntingBlock(Properties properties) {
        super(properties);
        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            BlockState state1 = state;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                //remove bunting shapes
                state1 = setConnection(dir, state1, hasConnection(dir, state1));
            }
            buntingToRope.put(state, state1);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    protected Map<BlockState, VoxelShape> makeShapes() {
        Map<BlockState, VoxelShape> shapes = new HashMap<>();

        VoxelShape down = Block.box(6, 0, 6, 10, 13, 10);
        VoxelShape up = Block.box(6, 9, 6, 10, 16, 10);
        VoxelShape north = Block.box(6, 9, 0, 10, 13, 10);
        VoxelShape south = Block.box(6, 9, 6, 10, 13, 16);
        VoxelShape west = Block.box(0, 9, 6, 10, 13, 10);
        VoxelShape east = Block.box(6, 9, 6, 16, 13, 10);
        VoxelShape knot = Block.box(6, 9, 6, 10, 13, 10);

        VoxelShape northBunting = Block.box(7, 0, 0, 9, 10, 8);
        VoxelShape southBunting = Block.box(7, 0, 8, 9, 10, 16);
        VoxelShape westBunting = Block.box(0, 0, 7, 8, 10, 9);
        VoxelShape eastBunting = Block.box(8, 0, 7, 16, 10, 9);


        for (BlockState state : this.stateDefinition.getPossibleStates()) {
            if (state.getValue(WATERLOGGED)) continue;
            VoxelShape v = Shapes.empty();
            if (state.getValue(KNOT)) v = Shapes.or(knot);
            if (state.getValue(DOWN)) v = Shapes.or(v, down);
            if (state.getValue(UP)) v = Shapes.or(v, up);
            ModBlockProperties.Bunting n = state.getValue(NORTH);
            if (n.isConnected()) v = Shapes.or(v, north);
            if (n.hasBunting()) v = Shapes.or(v, northBunting);
            ModBlockProperties.Bunting s = state.getValue(SOUTH);
            if (s.isConnected()) v = Shapes.or(v, south);
            if (s.hasBunting()) v = Shapes.or(v, southBunting);
            ModBlockProperties.Bunting w = state.getValue(WEST);
            if (w.isConnected()) v = Shapes.or(v, west);
            if (w.hasBunting()) v = Shapes.or(v, westBunting);
            ModBlockProperties.Bunting e = state.getValue(EAST);
            if (e.isConnected()) v = Shapes.or(v, east);
            if (e.hasBunting()) v = Shapes.or(v, eastBunting);
            v = v.optimize();
            boolean flag = true;
            for (VoxelShape existing : shapes.values()) {
                if (existing.equals(v)) {
                    shapes.put(state, existing);
                    flag = false;
                    break;
                }
            }
            if (flag) shapes.put(state, v);
        }
        return new Object2ObjectOpenHashMap<>(shapes);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (world.getBlockEntity(pos) instanceof ItemDisplayTile tile) {
                Containers.dropContents(world, pos, tile);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasConnection(Direction dir, BlockState state) {
        if (dir == Direction.DOWN) return state.getValue(DOWN);
        if (dir == Direction.UP) return state.getValue(UP);
        return state.getValue(HORIZONTAL_FACING_TO_PROPERTY_MAP.get(dir)).isConnected();
    }

    @Override
    public BlockState setConnection(Direction dir, BlockState state, boolean value) {
        if (dir == Direction.DOWN) return state.setValue(DOWN, value);
        if (dir == Direction.UP) return state.setValue(UP, value);
        return state.setValue(HORIZONTAL_FACING_TO_PROPERTY_MAP.get(dir),
                value ? ModBlockProperties.Bunting.ROPE : ModBlockProperties.Bunting.NONE);
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation, Direction axis, @Nullable Vec3 hit) {
        return Optional.of(state);
    }

    @Override
    public Optional<Direction> rotateOverAxis(BlockState state, LevelAccessor level, BlockPos pos, Rotation rotation, Direction axis, @Nullable Vec3 hit) {
        if (axis.getAxis() == Direction.Axis.Y) {
            if (level.getBlockEntity(pos) instanceof BuntingBlockTile tile) {
                Map<Direction, ItemStack> newMap = new HashMap<>();
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    ItemStack stack = tile.getItem(dir.get2DDataValue());
                    if (stack.isEmpty()) continue;
                    Direction newDir = rotation.rotate(dir);
                    if (canSupportBunting(state, newDir.get2DDataValue())) {
                        newMap.put(newDir, stack);
                    } else return Optional.empty();
                }
                if (!newMap.isEmpty()) {
                    tile.clearContent();
                    newMap.forEach((dir, stack) ->
                            tile.setItem(dir.get2DDataValue(), stack));
                    return Optional.of(axis);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return super.getCollisionShape(buntingToRope.get(state), worldIn, pos, context);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BuntingBlockTile(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof BuntingBlockTile tile && tile.isAccessibleBy(player)) {

            Vector3f hitPos = hit.getLocation()
                    .subtract(pos.getX() + 0.5, 0, pos.getZ() + 0.5).toVector3f();

            List<Direction> availableDir = Direction.Plane.HORIZONTAL.stream()
                    .filter(dir -> hasConnection(dir, state)).toList();

            // find index of closest vector
            var closest = availableDir.stream().min((a, b) -> {
                Vector3f v1 = a.step();
                Vector3f v2 = b.step();
                float d1 = v1.distanceSquared(hitPos);
                float d2 = v2.distanceSquared(hitPos);
                return Float.compare(d1, d2);
            });
            if (closest.isPresent()) return tile.interact(player, handIn, closest.get().get2DDataValue());
        }
        return InteractionResult.PASS;
    }


    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        var newState = super.updateShape(stateIn, facing, facingState, level, pos, facingPos);
        if (facing.getAxis().isHorizontal() &&
                hasConnection(facing, stateIn) &&
                level.getBlockEntity(pos) instanceof BuntingBlockTile tile) {
            int index = facing.get2DDataValue();
            ItemStack item = tile.getItem(index);
            if (!item.isEmpty() && !canSupportBunting(newState, index)) {
                if (level instanceof Level l) popItem(l, pos, item, facing);
                tile.setItem(index, ItemStack.EMPTY);
            }
            if (tile.isEmpty()) newState = ModRegistry.ROPE.get().withPropertiesOf(newState);
        }
        return newState;
    }

    public void popItem(Level level, BlockPos pos, ItemStack stack, Direction dir) {
        if (!level.isClientSide && !stack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            double h = EntityType.ITEM.getHeight() / 2.0 + 0.25;
            var step = dir.step().mul(0.25f);
            double x = step.x + pos.getX() + 0.5;
            double y = step.y + pos.getY() + 0.5 - h;
            double z = step.z + pos.getZ() + 0.5;
            ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack.copy());
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    public static boolean canSupportBunting(BlockState state, int index) {
        Direction dir = Direction.from2DDataValue(index);
        return state.getValue(HORIZONTAL_FACING_TO_PROPERTY_MAP.get(dir)).isConnected();
    }

    public static BlockState fromRope(BlockState state) {
        BuntingBlock block = ModRegistry.BUNTING_BLOCK.get();
        BlockState s = block.withPropertiesOf(state);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            s = block.setConnection(dir, s, ((RopeBlock) state.getBlock()).hasConnection(dir, state));
        }
        return s;
    }

    public static BlockState toRope(BlockState state) {
        RopeBlock block = ModRegistry.ROPE.get();
        BlockState s = block.withPropertiesOf(state);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            s = block.setConnection(dir, s, ((BuntingBlock) state.getBlock()).hasConnection(dir, state));
        }
        return s;
    }


}
