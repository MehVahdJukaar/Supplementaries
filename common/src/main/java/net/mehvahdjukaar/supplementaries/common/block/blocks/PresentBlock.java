package net.mehvahdjukaar.supplementaries.common.block.blocks;


import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.util.IColored;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PresentBlock extends WaterBlock implements EntityBlock, IColored {

    public static final BooleanProperty PACKED = BlockProperties.PACKED;

    private final DyeColor color;

    public static final VoxelShape SHAPE_LID = Block.box(1, 10, 1, 15, 14, 15);
    public static final VoxelShape SHAPE_OPEN = Block.box(2, 0, 2, 14, 10, 14);
    public static final VoxelShape SHAPE_CLOSED = Shapes.or(SHAPE_OPEN, SHAPE_LID);

    public PresentBlock(DyeColor color, Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState(this.stateDefinition.any().setValue(PACKED, false).setValue(WATERLOGGED, false));
    }

    @Nullable
    @Override
    public DyeColor getColor() {
        return color;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PresentBlockTile(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PACKED, WATERLOGGED);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        } else {
            if (worldIn.getBlockEntity(pos) instanceof PresentBlockTile tile) {
                return tile.interact((ServerPlayer) player, pos);
            }
        }
        return InteractionResult.PASS;
    }

    //overrides creative drop
    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (worldIn.getBlockEntity(pos) instanceof PresentBlockTile tile) {
            if (!worldIn.isClientSide && player.isCreative() && !tile.isEmpty()) {
                ItemStack itemstack = tile.getPresentItem(this);

                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                tile.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    //normal drop
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof PresentBlockTile tile) {
            ItemStack itemstack = tile.getPresentItem(this);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        ItemStack itemstack = super.getCloneItemStack(state, target, world, pos, player);
        if (world.getBlockEntity(pos) instanceof PresentBlockTile tile) {
            return tile.getPresentItem(this);
        }
        return itemstack;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            if (worldIn.getBlockEntity(pos) instanceof PresentBlockTile tile) {
                tile.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (state.getValue(PACKED))
            return SHAPE_CLOSED;
        return SHAPE_OPEN;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider ? (MenuProvider) blockEntity : null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state =  super.getStateForPlacement(context);
        CompoundTag tag = context.getItemInHand().getTag();
        if(tag != null && tag.contains("BlockEntityTag")){
            CompoundTag t = tag.getCompound("BlockEntityTag");
            if(t.contains("Items")) state = state.setValue(PACKED, true);
        }
        return state;
    }
}
