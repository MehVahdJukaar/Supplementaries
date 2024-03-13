package net.mehvahdjukaar.supplementaries.common.block.blocks;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BuntingBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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

    public BuntingBlock(Properties properties) {
        super(properties);
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

        VoxelShape northBunting = Block.box(0, 0, 0, 0, 10, 8);
        VoxelShape southBunting = Block.box(0, 0, 8, 0, 10, 16);
        VoxelShape westBunting = Block.box(0, 0, 0, 8, 10, 0);
        VoxelShape eastBunting = Block.box(8, 0, 0, 16, 10, 0);


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
    public boolean hasConnection(Direction dir, BlockState state) {
        if (dir == Direction.DOWN) return state.getValue(DOWN);
        if (dir == Direction.UP) return state.getValue(UP);
        return state.getValue(HORIZONTAL_FACING_TO_PROPERTY_MAP.get(dir)).isConnected();
    }

    @Override
    public BlockState setConnection(Direction dir, BlockState state, boolean value) {
        if (dir == Direction.DOWN) return state.setValue(DOWN, value);
        if (dir == Direction.UP) return state.setValue(UP, value);
        return state.setValue(HORIZONTAL_FACING_TO_PROPERTY_MAP.get(dir), ModBlockProperties.Bunting.ROPE);
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BuntingBlockTile(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof BuntingBlockTile tile && tile.isAccessibleBy(player)) {
            int ind;

            Vec3 v = hit.getLocation();
            v = v.subtract(pos.getX() + 0.5, 0, pos.getZ() + 0.5);

            if (v.x < v.z && v.x > -v.z) {
                ind = 0;
            } else if (v.x < v.z && v.x < -v.z) {
                ind = 1;
            } else if (v.x > v.z && v.x < -v.z) {
                ind = 2;
            } else {
                ind = 3;
            }
            return tile.interact(player, handIn, ind);
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

}
