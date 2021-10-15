package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.block.util.ILavaAndWaterLoggable;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.items.KeyItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.text.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class SafeBlock extends Block implements ILavaAndWaterLoggable {
    public static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LAVALOGGED = BlockProperties.LAVALOGGED;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public SafeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false)
                .setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(LAVALOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, FACING, WATERLOGGED, LAVALOGGED);
    }

    //schedule block tick
    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SafeBlockTile) {
            ((SafeBlockTile) tileentity).barrelTick();
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
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.LAVA, Fluids.LAVA.getTickDelay(worldIn));
        } else if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Fluid fluid = context.getLevel().getFluidState(context.getClickedPos()).getType();
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluid == Fluids.WATER).setValue(LAVALOGGED, fluid == Fluids.LAVA);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new SafeBlockTile();
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        } else {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof SafeBlockTile) {
                SafeBlockTile safe = ((SafeBlockTile) tileentity);
                ItemStack stack = player.getItemInHand(handIn);
                Item item = stack.getItem();

                //clear ownership with tripwire
                boolean cleared = false;
                if (ServerConfigs.cached.SAFE_SIMPLE) {
                    if ((item == Items.TRIPWIRE_HOOK || item instanceof KeyItem) &&
                            (safe.isOwnedBy(player) || (safe.isNotOwnedBy(player) && player.isCreative()))) {
                        cleared = true;
                    }
                } else {
                    if (player.isShiftKeyDown() && item instanceof KeyItem && (player.isCreative() ||
                            KeyLockableTile.isCorrectKey(stack, safe.password))) {
                        cleared = true;
                    }
                }

                if (cleared) {
                    safe.clearOwner();
                    player.displayClientMessage(new TranslatableComponent("message.supplementaries.safe.cleared"), true);
                    worldIn.playSound(null, pos,
                            SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, 1.5F);
                    return InteractionResult.CONSUME;
                }

                BlockPos p = pos.relative(state.getValue(FACING));
                if (!worldIn.getBlockState(p).isRedstoneConductor(worldIn, p)) {
                    if (ServerConfigs.cached.SAFE_SIMPLE) {
                        UUID owner = safe.owner;
                        if (owner == null) {
                            owner = player.getUUID();
                            safe.setOwner(owner);
                        }
                        if (!owner.equals(player.getUUID())) {
                            player.displayClientMessage(new TranslatableComponent("message.supplementaries.safe.owner", safe.ownerName), true);
                            if (!player.isCreative()) return InteractionResult.CONSUME;
                        }
                    } else {
                        String key = safe.password;
                        if (key == null) {
                            if (item instanceof KeyItem) {
                                safe.password = stack.getHoverName().getString();
                                player.displayClientMessage(new TranslatableComponent("message.supplementaries.safe.assigned_key", safe.password), true);
                                worldIn.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                        SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, 1.5F);
                                return InteractionResult.CONSUME;
                            }
                        } else if (!safe.canPlayerOpen(player, true) && !player.isCreative()) {
                            return InteractionResult.CONSUME;
                        }
                    }
                    player.openMenu((MenuProvider) tileentity);
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

        CompoundTag compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            if (ServerConfigs.cached.SAFE_SIMPLE) {
                if (compoundnbt.contains("Owner")) {
                    UUID id = compoundnbt.getUUID("Owner");
                    if (!id.equals(Minecraft.getInstance().player.getUUID())) {
                        String name = compoundnbt.getString("OwnerName");
                        tooltip.add((new TranslatableComponent("container.supplementaries.safe.owner", name)).withStyle(ChatFormatting.GRAY));
                        return;
                    }
                }
                if (compoundnbt.contains("LootTable", 8)) {
                    tooltip.add(new TextComponent("???????").withStyle(ChatFormatting.GRAY));
                }
                if (compoundnbt.contains("Items", 9)) {
                    NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                    ContainerHelper.loadAllItems(compoundnbt, nonnulllist);
                    int i = 0;
                    int j = 0;

                    for (ItemStack itemstack : nonnulllist) {
                        if (!itemstack.isEmpty()) {
                            ++j;
                            if (i <= 4) {
                                ++i;
                                MutableComponent iformattabletextcomponent = itemstack.getHoverName().copy();
                                iformattabletextcomponent.append(" x").append(String.valueOf(itemstack.getCount()));
                                tooltip.add(iformattabletextcomponent.withStyle(ChatFormatting.GRAY));
                            }
                        }
                    }

                    if (j - i > 0) {
                        tooltip.add((new TranslatableComponent("container.shulkerBox.more", j - i)).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
                    }
                }
                return;
            } else {
                if (compoundnbt.contains("Password")) {
                    tooltip.add((new TranslatableComponent("message.supplementaries.safe.bound")).withStyle(ChatFormatting.GRAY));
                    return;
                }
            }
        }
        tooltip.add((new TranslatableComponent("message.supplementaries.safe.unbound")).withStyle(ChatFormatting.GRAY));

    }

    public ItemStack getSafeItem(SafeBlockTile te) {
        CompoundTag compoundnbt = te.saveToNbt(new CompoundTag());
        ItemStack itemstack = new ItemStack(this.getBlock());
        if (!compoundnbt.isEmpty()) {
            itemstack.addTagElement("BlockEntityTag", compoundnbt);
        }

        if (te.hasCustomName()) {
            itemstack.setHoverName(te.getCustomName());
        }
        return itemstack;
    }


    //break protection
    @Override
    public boolean removedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (ServerConfigs.cached.SAFE_UNBREAKABLE) {
            BlockEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof SafeBlockTile) {
                if (!((SafeBlockTile) tileentity).canPlayerOpen(player, true)) return false;
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    //overrides creative drop
    @Override
    public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SafeBlockTile) {
            SafeBlockTile te = (SafeBlockTile) tileentity;
            if (!worldIn.isClientSide && player.isCreative() && !te.isEmpty()) {
                ItemStack itemstack = this.getSafeItem(te);

                ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                worldIn.addFreshEntity(itementity);
            } else {
                te.unpackLootTable(player);
            }
        }
        super.playerWillDestroy(worldIn, pos, state, player);
    }

    //TODO: use loot table instead
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        BlockEntity tileentity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (tileentity instanceof SafeBlockTile) {
            SafeBlockTile te = (SafeBlockTile) tileentity;
            ItemStack itemstack = this.getSafeItem(te);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        ItemStack itemstack = super.getPickBlock(state, target, world, pos, player);
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof SafeBlockTile) {
            return getSafeItem((SafeBlockTile) te);
        }
        return itemstack;
    }


    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SafeBlockTile) {
            if (stack.hasCustomHoverName()) {
                ((BaseContainerBlockEntity) tileentity).setCustomName(stack.getHoverName());
            }
            if (placer instanceof Player) {
                if (((SafeBlockTile) tileentity).owner == null)
                    ((SafeBlockTile) tileentity).setOwner(placer.getUUID());
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
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof SafeBlockTile) {
                worldIn.updateNeighbourForOutputSignal(pos, state.getBlock());
            }

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
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity instanceof MenuProvider ? (MenuProvider) tileentity : null;
    }


    @Override
    public FluidState getFluidState(BlockState state) {
        if (state.getValue(LAVALOGGED)) return Fluids.LAVA.getSource(false);
        else if (state.getValue(WATERLOGGED)) return Fluids.WATER.getSource(false);
        return super.getFluidState(state);
    }

    @Override
    public int getLightValue(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getValue(LAVALOGGED) ? 15 : 0;
    }

}
