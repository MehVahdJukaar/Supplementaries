package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.SignPostItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SignPostBlock extends FenceMimicBlock implements EntityBlock, IRotatable {

    private static final EnumProperty<Rotation> ROTATE_TILE = ModBlockProperties.ROTATE_TILE;

    public SignPostBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ROTATE_TILE, Rotation.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATE_TILE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof SignPostItem) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level instanceof ServerLevel serverLevel) {
            if (level.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
                return tile.handleInteraction(state, serverLevel, pos, player, hand, hitResult, stack);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            return ItemInteractionResult.SUCCESS;
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
            Block block = tile.getHeldBlock().getBlock();
            return block.getCloneItemStack(level, pos, block.defaultBlockState());
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @ForgeOverride
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof SignPostBlockTile tile) {
            var sign = tile.getClickedSign(target.getLocation());
            if (sign.active()) {
                return sign.getItem();
            } else return new ItemStack(tile.getHeldBlock().getBlock());
        }
        return new ItemStack(this);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SignPostBlockTile tile) {
            List<ItemStack> list = new ArrayList<>();
            list.add(new ItemStack(tile.getHeldBlock().getBlock()));
            var up = tile.getSignUp();
            var down = tile.getSignDown();
            if (up.active()) list.add(up.getItem());
            if (down.active()) list.add(down.getItem());

            return list;
        }
        return super.getDrops(state, builder);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        BlockState s = super.rotate(state, rotation);
        s.setValue(ROTATE_TILE, s.getValue(ROTATE_TILE).getRotated(rotation));
        return s;
    }

    //nobody uses this anyways we can get away with not having it...saves 4 states
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return super.mirror(state, mirror);
    }

    @ForgeOverride
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot) {
        return state;
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation, Direction axis, @Nullable Vec3 hit) {
        return Optional.of(state);
    }

    @Override
    public Optional<Direction> rotateOverAxis(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot, Direction axis, @Nullable Vec3 hit) {

        boolean success = false;
        if (world.getBlockEntity(pos) instanceof SignPostBlockTile tile) {

            boolean simple = hit == null;
            boolean ccw = rot.equals(Rotation.COUNTERCLOCKWISE_90);

            float angle = simple ? (ccw ? 90 : -90) : (22.5f * (ccw ? 1 : -1));

            if (simple) {
                if (tile.rotateSign(true, angle, false)) success = true;
                if (tile.rotateSign(false, angle, false)) success = true;
            } else {
                boolean up = hit.y % ((int) hit.y) > 0.5d;
                if (tile.rotateSign(up, angle, true)) success = true;
                else if (tile.rotateSign(!up, angle, true)) success = true;
            }

            if (success) {
                tile.setChanged();
                if (world instanceof Level level) {
                    level.sendBlockUpdated(pos, state, state, 3);
                }
                return Optional.of(Direction.UP);
            }
        }
        return Optional.empty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SignPostBlockTile(pPos, pState);
    }
}