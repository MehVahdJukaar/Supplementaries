package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.tiles.LunchBoxBlockTile;
import net.mehvahdjukaar.supplementaries.common.inventories.VariableSizeContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LunchBoxBlock extends WaterBlock implements EntityBlock {

    public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 7, 14);
    private static final VoxelShape SHAPE_HANGING = box(2, 4, 2, 14, 11, 14);

    public LunchBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(HANGING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANGING, FACING, OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(HANGING, context.getClickedFace() == Direction.DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ?  SHAPE_HANGING : SHAPE;
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
            if (worldIn.getBlockEntity(pos) instanceof LunchBoxBlockTile tile && player instanceof ServerPlayer sp) {
                VariableSizeContainerMenu.openTileMenu(sp, tile);
                PiglinAi.angerNearbyPiglins(player, true);

                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    //for creative drop
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof LunchBoxBlockTile tile) {
            if (!level.isClientSide && player.isCreative() && !tile.isEmpty()) {
                ItemStack itemstack = new ItemStack(this);
                saveTileToItem(itemstack, tile);

                ItemEntity itementity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            } else {
                tile.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof LunchBoxBlockTile tile) {
            ItemStack lunchBox = new ItemStack(this);
            saveTileToItem(lunchBox, tile);
            //TODO: 1.21: use loot tables copy nbt stuff
            return Collections.singletonList(lunchBox);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack itemstack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof LunchBoxBlockTile tile) {
            saveTileToItem(itemstack, tile);
        }
        return itemstack;
    }

    private static void saveTileToItem(ItemStack itemstack, LunchBoxBlockTile tile) {
        var data = LunchBoxItem.getLunchBoxData(itemstack);
        if (data != null) {
            for (int inx = 0; inx < tile.getContainerSize(); inx++) {
                ItemStack food = tile.getItem(inx);
                data.tryAdding(food);
            }
        } else Supplementaries.error();
        if (tile.hasCustomName()) {
            itemstack.setHoverName(tile.getCustomName());
        }
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (worldIn.getBlockEntity(pos) instanceof LunchBoxBlockTile tile) {
            if (stack.hasCustomHoverName()) {
                tile.setCustomName(stack.getHoverName());
            }
            var data = LunchBoxItem.getLunchBoxData(stack);
            if (data != null) {
                int index = 0;
                for (var i : data.getContentView()) {
                    tile.setItem(index++, i.copy());
                }
            } else Supplementaries.error();
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
        if (worldIn.getBlockEntity(pos) instanceof Container tile) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer(tile);
        }
        return 0;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        return worldIn.getBlockEntity(pos) instanceof MenuProvider menuProvider ? menuProvider : null;
    }

}
