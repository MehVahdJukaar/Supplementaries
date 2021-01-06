package net.mehvahdjukaar.supplementaries.blocks.tiles;

import io.netty.buffer.Unpooled;
import net.mehvahdjukaar.supplementaries.blocks.SackBlock;
import net.mehvahdjukaar.supplementaries.common.Resources;
import net.mehvahdjukaar.supplementaries.gui.SackContainer;
import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class SackBlockTile extends LockableLootTileEntity implements ISidedInventory {

    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int numPlayersUsing;

    public SackBlockTile() {
        super(Registry.SACK_TILE);
    }

    @Override
    public int getSizeInventory() {
        return this.items.size();
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.sack");
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
            BlockState blockstate = this.getBlockState();
            boolean flag = blockstate.get(SackBlock.OPEN);
            if (!flag) {
                this.world.playSound(null, this.pos.getX()+0.5, this.pos.getY()+0.5, this.pos.getZ()+0.5,
                        SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.55F);
                this.world.setBlockState(this.getPos(), blockstate.with(SackBlock.OPEN, true), 3);
            }
            this.world.getPendingBlockTicks().scheduleTick(this.getPos(), this.getBlockState().getBlock(), 5);
        }
    }
    public static int calculatePlayersUsing(World world, LockableTileEntity tile, int x, int y, int z) {
        int i = 0;
        for(PlayerEntity playerentity : world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB((float)x - 5.0F, (float)y - 5.0F, (float)z - 5.0F, (float)(x + 1) + 5.0F, (float)(y + 1) + 5.0F, (float)(z + 1) + 5.0F))) {
            if (playerentity.openContainer instanceof SackContainer) {
                IInventory iinventory = ((SackContainer)playerentity.openContainer).inventory;
                if (iinventory == tile) {
                    ++i;
                }
            }
        }
        return i;
    }

    public void barrelTick() {
        int i = this.pos.getX();
        int j = this.pos.getY();
        int k = this.pos.getZ();
        this.numPlayersUsing = calculatePlayersUsing(this.world, this, i, j, k);
        if (this.numPlayersUsing > 0) {
            this.world.getPendingBlockTicks().scheduleTick(this.getPos(), this.getBlockState().getBlock(), 5);
        } else {
            BlockState blockstate = this.getBlockState();
            /*
            if (!blockstate.isIn(Blocks.BARREL)) {
                this.remove();
                return;
            }*/

            boolean flag = blockstate.get(SackBlock.OPEN);
            if (flag) {
                //this.playSound(blockstate, SoundEvents.BLOCK_BARREL_CLOSE);
                this.world.playSound((PlayerEntity)null, this.pos.getX()+0.5, this.pos.getY()+0.5, this.pos.getZ()+0.5,
                        SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.5F);
                this.world.setBlockState(this.getPos(), blockstate.with(SackBlock.OPEN, false), 3);
            }
        }

    }

    @Override
    public void closeInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        this.loadFromNbt(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        return this.saveToNbt(compound);
    }

    public void loadFromNbt(CompoundNBT compound) {
        this.items = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(compound) && compound.contains("Items", 9)) {
            ItemStackHelper.loadAllItems(compound, this.items);
        }
    }

    public CompoundNBT saveToNbt(CompoundNBT compound) {
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.items, false);
        }
        return compound;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.items = itemsIn;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return new SackContainer(id, player, new PacketBuffer(Unpooled.buffer()).writeBlockPos(this.getPos()), this);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(), pkt.getNbtCompound());
    }


    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        ITag<Item> t = ItemTags.getCollection().get(Resources.SHULKER_BLACKLIST);
        if(t!=null && stack.getItem().isIn(t))
            return false;
        return super.isItemValidForSlot(index,stack);
    }


    //TODO: figure out what this handlers and ISided inventory do
    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getSizeInventory()).toArray();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, @Nullable Direction direction) {
        return this.isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return true;
    }
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.removed && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return handlers[facing.ordinal()].cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void remove() {
        super.remove();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
    }

}
