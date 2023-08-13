package net.mehvahdjukaar.supplementaries.common.block.blocks;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.IRotatable;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.DoubleSkullBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Deprecated(forRemoval = true)
public class DoubleSkullBlock extends SkullBlock implements IRotatable {
    protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public DoubleSkullBlock(Properties properties) {
        super(Types.SKELETON, properties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DoubleSkullBlockTile(pPos, pState);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof DoubleSkullBlockTile tile) {
            List<ItemStack> loot = new ArrayList<>();
            BlockEntity skullTile = tile.getSkullTile();
            if(skullTile!=null){
                BlockState skull = skullTile.getBlockState();
                builder = builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, skullTile);

                loot.addAll(skull.getDrops(builder));
            }
            BlockEntity skullTileUp = tile.getSkullTileUp();
            if(skullTileUp!=null){
                BlockState skull = skullTileUp.getBlockState();
                builder = builder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, skullTileUp)
                        .withOptionalParameter(LootContextParams.BLOCK_STATE, skull);
                loot.addAll(skull.getDrops(builder));
            }
            return loot;
        }
        return super.getDrops(pState, builder);
    }

    //crappy for fabric
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return super.getCloneItemStack(level, pos, state);
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public ItemStack getCloneItemStack(BlockState state, HitResult hitResult, BlockGetter world, BlockPos pos, Player player) {
        if (world.getBlockEntity(pos) instanceof DoubleSkullBlockTile tile) {
            double y = hitResult.getLocation().y;
            boolean up = y % ((int) y) > 0.5d;
            return up ? tile.getSkullItemUp() : tile.getSkullItem();
        }
        return super.getCloneItemStack(world, pos, state);
    }

    @Override
    public Optional<BlockState> getRotatedState(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation, Direction axis, @Nullable Vec3 hit) {
        return Optional.of(this.rotate(state, rotation));
    }

    @Override
    public Optional<Direction> rotateOverAxis(BlockState state, LevelAccessor world, BlockPos pos, Rotation rot, Direction axis, @Nullable Vec3 hit) {
        if (world.getBlockEntity(pos) instanceof DoubleSkullBlockTile tile) {

            boolean simple = hit == null;

            if (simple) {
                tile.rotateUp(rot);
                IRotatable.super.rotateOverAxis(state, world, pos, rot, axis, null);
            } else {
                boolean up = hit.y % ((int) hit.y) > 0.5d;
                int inc = rot == Rotation.CLOCKWISE_90 ? -1 : 1;
                if (up) tile.rotateUpStep(inc);
                else {
                    if (world instanceof ServerLevel) {
                        world.setBlock(pos, state.setValue(ROTATION, (state.getValue(ROTATION) - inc + 16) % 16), 11);
                    }
                }
            }
            tile.setChanged();
            if (world instanceof Level level) {
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return Optional.of(Direction.UP);

        }
        return Optional.empty();
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction dir, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        if (dir == Direction.UP && pLevel.getBlockEntity(pCurrentPos) instanceof DoubleSkullBlockTile tile) {
            tile.updateWax(pNeighborState);
        }
        return super.updateShape(pState, dir, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return Utils.getTicker(type, ModRegistry.SKULL_PILE_TILE.get(), DoubleSkullBlockTile::ti2ck);
    }
}
