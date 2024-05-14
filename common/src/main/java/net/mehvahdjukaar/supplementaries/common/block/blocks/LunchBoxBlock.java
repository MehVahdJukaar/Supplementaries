package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.LunchBoxBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SackBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LunchBoxBlock extends WaterBlock implements EntityBlock {

    public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public LunchBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(OPEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new LunchBoxBlockTile(pPos, pState);
    }


    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        } else {
            if (worldIn.getBlockEntity(pos) instanceof LunchBoxBlockTile tile) {

                PlatHelper.openCustomMenu((ServerPlayer) player, tile, p -> {
                    p.writeBoolean(true);
                    p.writeBlockPos(pos);
                    p.writeInt(tile.getContainerSize());
                });
                PiglinAi.angerNearbyPiglins(player, true);

                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    //for creative drop
    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (worldIn.getBlockEntity(pos) instanceof LunchBoxBlockTile tile) {
            if (!worldIn.isClientSide && player.isCreative()) {
                CompoundTag compoundTag = tile.saveWithoutMetadata();
                ItemStack itemstack = new ItemStack(this);
                if (!compoundTag.isEmpty()) {
                    itemstack.addTagElement("BlockEntityTag", compoundTag);
                }

                if (tile.hasCustomName()) {
                    itemstack.setHoverName(tile.getCustomName());
                }

                ItemEntity itementity = new ItemEntity(worldIn, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                tile.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SackBlockTile tile) {
            builder = builder.withDynamicDrop(CONTENTS, (context) -> {
                for (int i = 0; i < tile.getContainerSize(); ++i) {
                    context.accept(tile.getItem(i));
                }
            });
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack itemstack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof SackBlockTile tile) {
            CompoundTag compoundTag = tile.saveWithoutMetadata();
            if (!compoundTag.isEmpty()) {
                itemstack.addTagElement("BlockEntityTag", compoundTag);
            }
        }
        return itemstack;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            if (worldIn.getBlockEntity(pos) instanceof SackBlockTile tile) {
                tile.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        if (worldIn.getBlockEntity(pos) instanceof SackBlockTile tile) {
            int i = 0;
            float f = 0.0F;
            int slots = SackBlockTile.getUnlockedSlots();
            for (int j = 0; j < slots; ++j) {
                ItemStack itemstack = tile.getItem(j);
                if (!itemstack.isEmpty()) {
                    f += itemstack.getCount() / (float) Math.min(tile.getMaxStackSize(), itemstack.getMaxStackSize());
                    ++i;
                }
            }
            f = f / slots;
            return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
        return 0;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        return worldIn.getBlockEntity(pos) instanceof MenuProvider menuProvider ? menuProvider : null;
    }

}
