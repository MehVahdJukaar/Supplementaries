package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.KeyLockableTile;
import net.mehvahdjukaar.supplementaries.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.items.KeyItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.TripWireHookBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.AbstractBlock.Properties;

public class SafeBlock extends Block implements IWaterLoggable{
    public static final VoxelShape SHAPE = Block.box(1,0,1,15,16,15);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public SafeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false).setValue(FACING, Direction.NORTH).setValue(WATERLOGGED,false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(OPEN,FACING,WATERLOGGED);
    }

    //schedule block tick
    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SafeBlockTile) {
            ((SafeBlockTile)tileentity).barrelTick();
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
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, flag);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SafeBlockTile();
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isClientSide) {
            return ActionResultType.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResultType.CONSUME;
        } else {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof SafeBlockTile) {
                SafeBlockTile safe = ((SafeBlockTile) tileentity);
                ItemStack stack = player.getItemInHand(handIn);
                Item item = stack.getItem();

                //clear ownership with tripwire
                boolean cleared = false;
                if(ServerConfigs.cached.SAFE_SIMPLE){
                    if(((item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof TripWireHookBlock)||
                            item instanceof KeyItem) &&
                            (safe.isOwnedBy(player)||(safe.isNotOwnedBy(player)&&player.isCreative()))){
                        cleared = true;
                    }
                }
                else{
                    if(player.isShiftKeyDown() && item instanceof KeyItem && (player.isCreative() ||
                            stack.getHoverName().getString().equals(safe.password))){
                        cleared = true;
                    }
                }
                if(cleared){
                    safe.clearOwner();
                    player.displayClientMessage(new TranslationTextComponent("message.supplementaries.safe.cleared"),true);
                    worldIn.playSound(null, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5,
                            SoundEvents.IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5F, 1.5F);
                    return ActionResultType.CONSUME;
                }

                BlockPos p = pos.relative(state.getValue(FACING));
                if (!worldIn.getBlockState(p).isRedstoneConductor(worldIn, p)){
                    if(ServerConfigs.cached.SAFE_SIMPLE) {
                        UUID owner = safe.owner;
                        if (owner == null) {
                            owner = player.getUUID();
                            safe.setOwner(owner);
                        }
                        if (!owner.equals(player.getUUID())) {
                            player.displayClientMessage(new TranslationTextComponent("message.supplementaries.safe.owner", safe.ownerName), true);
                            if (!player.isCreative()) return ActionResultType.CONSUME;
                        }
                    }
                    else{
                        String key = safe.password;
                        if(key==null){
                            if(item instanceof KeyItem){
                                safe.password=stack.getHoverName().getString();
                                player.displayClientMessage(new TranslationTextComponent("message.supplementaries.safe.assigned_key",safe.password), true);
                                worldIn.playSound(null, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5,
                                        SoundEvents.IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5F, 1.5F);
                                return ActionResultType.CONSUME;
                            }
                        }
                        else if(!KeyLockableTile.isKeyInInventory(player, safe.password,"safe")&&!player.isCreative()){
                            return ActionResultType.CONSUME;
                        }
                    }
                    player.openMenu((INamedContainerProvider) tileentity);
                    PiglinTasks.angerNearbyPiglins(player, true);
                }

                return ActionResultType.CONSUME;
            } else {
                return ActionResultType.PASS;
            }
        }
    }



    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            if(ServerConfigs.cached.SAFE_SIMPLE) {
                if (compoundnbt.contains("Owner")) {
                    UUID id = compoundnbt.getUUID("Owner");
                    if (!id.equals(Minecraft.getInstance().player.getUUID())) {
                        String name = compoundnbt.getString("OwnerName");
                        tooltip.add((new TranslationTextComponent("container.supplementaries.safe.owner", name)).withStyle(TextFormatting.GRAY));
                        return;
                    }
                }
                if (compoundnbt.contains("LootTable", 8)) {
                    tooltip.add(new StringTextComponent("???????").withStyle(TextFormatting.GRAY));
                }
                if (compoundnbt.contains("Items", 9)) {
                    NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                    ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                    int i = 0;
                    int j = 0;

                    for (ItemStack itemstack : nonnulllist) {
                        if (!itemstack.isEmpty()) {
                            ++j;
                            if (i <= 4) {
                                ++i;
                                IFormattableTextComponent iformattabletextcomponent = itemstack.getHoverName().copy();
                                iformattabletextcomponent.append(" x").append(String.valueOf(itemstack.getCount()));
                                tooltip.add(iformattabletextcomponent.withStyle(TextFormatting.GRAY));
                            }
                        }
                    }

                    if (j - i > 0) {
                        tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY));
                    }
                }
                return;
            }
            else{
                if (compoundnbt.contains("Password")) {
                    tooltip.add((new TranslationTextComponent("message.supplementaries.safe.bound")).withStyle(TextFormatting.GRAY));
                    return;
                }
            }
        }
        tooltip.add((new TranslationTextComponent("message.supplementaries.safe.unbound")).withStyle(TextFormatting.GRAY));

    }

    public ItemStack getSafeItem(SafeBlockTile te) {
        CompoundNBT compoundnbt = te.saveToNbt(new CompoundNBT());
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
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if(ServerConfigs.cached.SAFE_UNBREAKABLE) {
            TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof SafeBlockTile) {
                SafeBlockTile te = (SafeBlockTile) tileentity;
                if(ServerConfigs.cached.SAFE_SIMPLE) {
                    if (!player.isCreative() && te.isNotOwnedBy(player)) {
                        player.displayClientMessage(new TranslationTextComponent("message.supplementaries.safe.owner", te.ownerName), true);
                        return false;
                    }
                }
                else{
                    if (!player.isCreative() && !KeyLockableTile.isKeyInInventory(player,te.password,"safe")) {
                        player.displayClientMessage(new TranslationTextComponent("message.supplementaries.safe.locked", te.ownerName), true);
                        return false;
                    }
                }
            }
        }
        return super.removedByPlayer(state,world,pos,player,willHarvest,fluid);
    }

    //overrides creative drop
    @Override
    public void playerWillDestroy(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SafeBlockTile) {
            SafeBlockTile te = (SafeBlockTile)tileentity;
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

    //normal drop
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof SafeBlockTile) {
            SafeBlockTile te = (SafeBlockTile)tileentity;
            ItemStack itemstack = this.getSafeItem(te);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    //pick block. TODO: use getsafe item here. clean up
    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack itemstack = super.getCloneItemStack(world, pos, state);
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof SafeBlockTile){
            CompoundNBT compoundnbt = ((SafeBlockTile)te).saveToNbt(new CompoundNBT());
            if (!compoundnbt.isEmpty()) {
                itemstack.addTagElement("BlockEntityTag", compoundnbt);
            }
        }
        return itemstack;
    }



    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof SafeBlockTile) {
            if (stack.hasCustomHoverName()) {
                ((LockableTileEntity) tileentity).setCustomName(stack.getHoverName());
            }
            if (placer instanceof PlayerEntity) {
                if(((SafeBlockTile) tileentity).owner==null)
                   ((SafeBlockTile) tileentity).setOwner(placer.getUUID());
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
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
    public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
        return Container.getRedstoneSignalFromContainer((IInventory)worldIn.getBlockEntity(pos));
    }

    @Override
    public INamedContainerProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider)tileentity : null;
    }



}
