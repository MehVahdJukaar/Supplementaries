package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BuntingBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BuntingBlock extends RopeBlock implements EntityBlock, IRotatable {

    public BuntingBlock(Properties properties) {
        super(properties);
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
                stateIn.getValue(FACING_TO_PROPERTY_MAP.get(facing)) &&
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
        return state.getValue(BuntingBlock.FACING_TO_PROPERTY_MAP.get(dir));
    }

}
