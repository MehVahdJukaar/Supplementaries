package net.mehvahdjukaar.supplementaries.blocks;

import net.mehvahdjukaar.supplementaries.blocks.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.blocks.tiles.SackBlockTile;
import net.mehvahdjukaar.supplementaries.blocks.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SafeBlock extends Block {
    public static final VoxelShape SHAPE = Block.makeCuboidShape(1,0,1,15,16,15);

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public SafeBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState().with(OPEN, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
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
                UUID owner = safe.owner;
                if(owner==null){
                    owner=player.getUniqueID();
                    safe.setOwner(owner);
                }
                if(owner.equals(player.getUniqueID())) {
                    player.openContainer((INamedContainerProvider) tileentity);
                    PiglinTasks.func_234478_a_(player, true);
                }
                else{
                    player.sendStatusMessage(new TranslationTextComponent("message.supplementaries.safe")
                                    .appendString(" "+safe.ownerName), true);
                }

                return ActionResultType.CONSUME;
            } else {
                return ActionResultType.PASS;
            }
        }
    }
    //TODO: camelcase all nbts 4 consistency
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        if (compoundnbt != null) {
            if(compoundnbt.contains("Owner")){
                UUID id = compoundnbt.getUniqueId("Owner");
                if(!id.equals(Minecraft.getInstance().player.getUniqueID())){
                    String name = compoundnbt.getString("OwnerName");
                    tooltip.add((new TranslationTextComponent("container.supplementaries.safe").appendString(" "+name)).mergeStyle(TextFormatting.GRAY));
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

    //overrides creative drop
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SafeBlockTile) {
            SafeBlockTile te = (SafeBlockTile)tileentity;
            if (!worldIn.isRemote && player.isCreative() && !te.isEmpty()) {

                ItemStack itemstack = this.getSafeItem(te);

                ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
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
