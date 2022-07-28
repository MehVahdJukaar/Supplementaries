package net.mehvahdjukaar.supplementaries.common.block.blocks;

import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.ILavaAndWaterLoggable;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SafeBlock extends Block implements ILavaAndWaterLoggable, EntityBlock {
    public static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LAVALOGGED = ModBlockProperties.LAVALOGGED;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public SafeBlock(Properties properties) {
        super(properties.lightLevel(state->state.getValue(LAVALOGGED) ? 15 : 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false)
                .setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(LAVALOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, FACING, WATERLOGGED, LAVALOGGED);
    }

    //schedule block tick
    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        if (worldIn.getBlockEntity(pos) instanceof SafeBlockTile tile) {
            tile.recheckOpen();
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(LAVALOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.LAVA, Fluids.LAVA.getTickDelay(worldIn));
        } else if (stateIn.getValue(WATERLOGGED)) {
            worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        Fluid fluid = fluidState.getType();
        boolean full = fluidState.getAmount() == 8;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, full && fluid == Fluids.WATER)
                .setValue(LAVALOGGED, full && fluid == Fluids.LAVA);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SafeBlockTile(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        } else {
            if (worldIn.getBlockEntity(pos) instanceof SafeBlockTile tile) {
                ItemStack stack = player.getItemInHand(handIn);
                Item item = stack.getItem();

                //clear ownership with tripwire
                boolean cleared = false;
                if (CommonConfigs.Blocks.SAFE_SIMPLE.get()) {
                    if ((item == Items.TRIPWIRE_HOOK || stack.is(ModTags.KEY)) &&
                            (tile.isOwnedBy(player) || (tile.isNotOwnedBy(player) && player.isCreative()))) {
                        cleared = true;
                    }
                } else {
                    if (player.isShiftKeyDown() && stack.is(ModTags.KEY) && (player.isCreative() ||
                            KeyLockableTile.isCorrectKey(stack, tile.password))) {
                        cleared = true;
                    }
                }

                if (cleared) {
                    tile.clearOwner();
                    player.displayClientMessage(Component.translatable("message.supplementaries.safe.cleared"), true);
                    worldIn.playSound(null, pos,
                            SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, 1.5F);
                    return InteractionResult.CONSUME;
                }

                BlockPos p = pos.relative(state.getValue(FACING));
                if (!worldIn.getBlockState(p).isRedstoneConductor(worldIn, p)) {
                    if (CommonConfigs.Blocks.SAFE_SIMPLE.get()) {
                        UUID owner = tile.owner;
                        if (owner == null) {
                            owner = player.getUUID();
                            tile.setOwner(owner);
                        }
                        if (!owner.equals(player.getUUID())) {
                            player.displayClientMessage(Component.translatable("message.supplementaries.safe.owner", tile.ownerName), true);
                            if (!player.isCreative()) return InteractionResult.CONSUME;
                        }
                    } else {
                        String key = tile.password;
                        if (key == null) {
                            if (stack.is(ModTags.KEY)) {
                                tile.password = stack.getHoverName().getString();
                                player.displayClientMessage(Component.translatable("message.supplementaries.safe.assigned_key", tile.password), true);
                                worldIn.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, 1.5F);
                                return InteractionResult.CONSUME;
                            }
                        } else if (!tile.canPlayerOpen(player, true) && !player.isCreative()) {
                            return InteractionResult.CONSUME;
                        }
                    }
                    player.openMenu(tile);
                    PiglinAi.angerNearbyPiglins(player, true);
                }

                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        CompoundTag compoundTag = stack.getTagElement("BlockEntityTag");
        if (compoundTag != null) {
            if (CommonConfigs.Blocks.SAFE_SIMPLE.get()) {
                if (compoundTag.contains("Owner")) {
                    UUID id = compoundTag.getUUID("Owner");
                    if (!id.equals(Minecraft.getInstance().player.getUUID())) {
                        String name = compoundTag.getString("OwnerName");
                        tooltip.add((Component.translatable("container.supplementaries.safe.owner", name)).withStyle(ChatFormatting.GRAY));
                        return;
                    }
                }
                if (compoundTag.contains("LootTable", 8)) {
                    tooltip.add(Component.literal("???????").withStyle(ChatFormatting.GRAY));
                }
                if (compoundTag.contains("Items", 9)) {
                    NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
                    ContainerHelper.loadAllItems(compoundTag, itemStacks);
                    int i = 0;
                    int j = 0;

                    for (ItemStack itemstack : itemStacks) {
                        if (!itemstack.isEmpty()) {
                            ++j;
                            if (i <= 4) {
                                ++i;
                                MutableComponent component = itemstack.getHoverName().copy();
                                component.append(" x").append(String.valueOf(itemstack.getCount()));
                                tooltip.add(component.withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }

                    if (j - i > 0) {
                        tooltip.add((Component.translatable("container.shulkerBox.more", j - i)).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
                    }
                }
                return;
            } else {
                if (compoundTag.contains("Password")) {
                    tooltip.add((Component.translatable("message.supplementaries.safe.bound")).withStyle(ChatFormatting.GRAY));
                    return;
                }
            }
        }
        tooltip.add((Component.translatable("message.supplementaries.safe.unbound")).withStyle(ChatFormatting.GRAY));
    }

    public ItemStack getSafeItem(SafeBlockTile te) {
        CompoundTag compoundTag = te.saveWithoutMetadata();
        ItemStack itemstack = new ItemStack(this);
        if (!compoundTag.isEmpty()) {
            itemstack.addTagElement("BlockEntityTag", compoundTag);
        }
        if (te.hasCustomName()) {
            itemstack.setHoverName(te.getCustomName());
        }
        return itemstack;
    }

    //overrides creative drop
    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (worldIn.getBlockEntity(pos) instanceof SafeBlockTile tile) {
            if (!worldIn.isClientSide && player.isCreative() && !tile.isEmpty()) {
                ItemStack itemstack = this.getSafeItem(tile);

                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                tile.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    //TODO: use loot table instead
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SafeBlockTile tile) {
            ItemStack itemstack = this.getSafeItem(tile);
            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack itemstack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof SafeBlockTile tile) {
            return getSafeItem(tile);
        }
        return itemstack;
    }


    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (worldIn.getBlockEntity(pos) instanceof SafeBlockTile tile) {
            if (stack.hasCustomHoverName()) {
                tile.setCustomName(stack.getHoverName());
            }
            if (placer instanceof Player) {
                if (tile.owner == null)
                    tile.setOwner(placer.getUUID());
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider ? (MenuProvider) blockEntity : null;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.getValue(LAVALOGGED)) return Fluids.LAVA.getSource(false);
        else if (state.getValue(WATERLOGGED)) return Fluids.WATER.getSource(false);
        return super.getFluidState(state);
    }

}
