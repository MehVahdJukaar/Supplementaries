package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.blocks.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
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

public class SafeBlock extends Block implements IWaterLoggable{
    public static final VoxelShape SHAPE = Block.makeCuboidShape(1,0,1,15,16,15);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public SafeBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(OPEN, false).with(FACING, Direction.NORTH).with(WATERLOGGED,false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(OPEN,FACING,WATERLOGGED);
    }

    //schedule block tick
    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SafeBlockTile) {
            ((SafeBlockTile)tileentity).barrelTick();
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        boolean flag = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite()).with(WATERLOGGED, flag);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
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
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return ActionResultType.SUCCESS;
        } else if (player.isSpectator()) {
            return ActionResultType.CONSUME;
        } else {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof SafeBlockTile) {
                SafeBlockTile safe = ((SafeBlockTile) tileentity);
                Item item = player.getHeldItem(handIn).getItem();
                //clear ownership with tripwire
                if(item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof TripWireHookBlock &&
                        (safe.isOwnedBy(player)||(safe.isNotOwnedBy(player)&&player.isCreative()))){
                    safe.clearOwner();
                    player.sendStatusMessage(new TranslationTextComponent("message.supplementaries.safe.cleared"),true);
                    worldIn.playSound(null, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5,
                            SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5F, 1.5F);
                    return ActionResultType.CONSUME;
                }
                BlockPos p = pos.offset(state.get(FACING));
                if (!worldIn.getBlockState(p).isNormalCube(worldIn, p)){
                    UUID owner = safe.owner;
                    if (owner == null) {
                        owner = player.getUniqueID();
                        safe.setOwner(owner);
                    }
                    if (!owner.equals(player.getUniqueID())) {
                        player.sendStatusMessage(new TranslationTextComponent("message.supplementaries.safe.owner",safe.ownerName), true);
                        if (!player.isCreative()) return ActionResultType.CONSUME;
                    }
                    player.openContainer((INamedContainerProvider) tileentity);
                    PiglinTasks.func_234478_a_(player, true);
                }

                return ActionResultType.CONSUME;
            } else {
                return ActionResultType.PASS;
            }
        }
    }
    //TODO: camelcase all nbts 4 consistency

    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt != null) {
            if(compoundnbt.contains("Owner")){
                UUID id = compoundnbt.getUniqueId("Owner");
                if(!id.equals(Minecraft.getInstance().player.getUniqueID())){
                    String name = compoundnbt.getString("OwnerName");
                    tooltip.add((new TranslationTextComponent("container.supplementaries.safe.owner", name)).mergeStyle(TextFormatting.GRAY));
                    return;
                }
            }
            if (compoundnbt.contains("LootTable", 8)) {
                tooltip.add(new StringTextComponent("???????").mergeStyle(TextFormatting.GRAY));
            }
            if (compoundnbt.contains("Items", 9)) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                int i = 0;
                int j = 0;

                for(ItemStack itemstack : nonnulllist) {
                    if (!itemstack.isEmpty()) {
                        ++j;
                        if (i <= 4) {
                            ++i;
                            IFormattableTextComponent iformattabletextcomponent = itemstack.getDisplayName().deepCopy();
                            iformattabletextcomponent.appendString(" x").appendString(String.valueOf(itemstack.getCount()));
                            tooltip.add(iformattabletextcomponent.mergeStyle(TextFormatting.GRAY));
                        }
                    }
                }

                if (j - i > 0) {
                    tooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).mergeStyle(TextFormatting.ITALIC).mergeStyle(TextFormatting.GRAY));
                }
            }
        }
        tooltip.add((new TranslationTextComponent("message.supplementaries.safe.unbound")).mergeStyle(TextFormatting.GRAY));

    }

    public ItemStack getSafeItem(SafeBlockTile te) {
        CompoundNBT compoundnbt = te.saveToNbt(new CompoundNBT());
        ItemStack itemstack = new ItemStack(this.getBlock());
        if (!compoundnbt.isEmpty()) {
            itemstack.setTagInfo("BlockEntityTag", compoundnbt);
        }

        if (te.hasCustomName()) {
            itemstack.setDisplayName(te.getCustomName());
        }
        return itemstack;
    }


    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if(ServerConfigs.cached.SAFE_UNBREAKABLE) {
            TileEntity tileentity = world.getTileEntity(pos);
            if (tileentity instanceof SafeBlockTile) {
                SafeBlockTile te = (SafeBlockTile) tileentity;
                if (!player.isCreative() && te.isNotOwnedBy(player)) {
                    player.sendStatusMessage(new TranslationTextComponent("message.supplementaries.safe.owner",te.ownerName), true);
                    return false;
                }
            }
        }
        return super.removedByPlayer(state,world,pos,player,willHarvest,fluid);
    }

    //overrides creative drop
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SafeBlockTile) {
            SafeBlockTile te = (SafeBlockTile)tileentity;
            if (!worldIn.isRemote && player.isCreative() && !te.isEmpty()) {
                    ItemStack itemstack = this.getSafeItem(te);

                    ItemEntity itementity = new ItemEntity(worldIn, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, itemstack);
                    itementity.setDefaultPickupDelay();
                    worldIn.addEntity(itementity);
            } else {
                te.fillWithLoot(player);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    //normal drop
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof SafeBlockTile) {
            SafeBlockTile te = (SafeBlockTile)tileentity;
            ItemStack itemstack = this.getSafeItem(te);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    //pick block
    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        ItemStack itemstack = super.getItem(worldIn, pos, state);
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof SafeBlockTile){
            CompoundNBT compoundnbt = ((SafeBlockTile)te).saveToNbt(new CompoundNBT());
            if (!compoundnbt.isEmpty()) {
                itemstack.setTagInfo("BlockEntityTag", compoundnbt);
            }
        }
        return itemstack;
    }



    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SafeBlockTile) {
            if (stack.hasDisplayName()) {
                ((SafeBlockTile) tileentity).setCustomName(stack.getDisplayName());
            }
            if (placer instanceof PlayerEntity) {
                if(((SafeBlockTile) tileentity).owner==null)
                   ((SafeBlockTile) tileentity).setOwner(placer.getUniqueID());
            }
        }
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isIn(newState.getBlock())) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof SafeBlockTile) {
                worldIn.updateComparatorOutputLevel(pos, state.getBlock());
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        return Container.calcRedstoneFromInventory((IInventory)worldIn.getTileEntity(pos));
    }

    @Override
    public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
        super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(id, param);
    }

    @Override
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider)tileentity : null;
    }



}
