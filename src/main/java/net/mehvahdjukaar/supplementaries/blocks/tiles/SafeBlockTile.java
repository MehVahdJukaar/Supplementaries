package net.mehvahdjukaar.supplementaries.blocks.tiles;

import io.netty.buffer.Unpooled;
import net.mehvahdjukaar.supplementaries.blocks.SackBlock;
import net.mehvahdjukaar.supplementaries.blocks.SafeBlock;
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
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.stream.IntStream;

public class SafeBlockTile extends LockableLootTileEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int numPlayersUsing;

    public String ownerName = null;
    public UUID owner = null;

    public SafeBlockTile() {
        super(Registry.SAFE_TILE);
    }

    @Override
    public int getSizeInventory() {
        return this.items.size();
    }

    public void setOwner(UUID owner){
        this.ownerName=world.getPlayerByUuid(owner).getName().getString();
        this.owner=owner;
        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.safe");
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
            BlockState blockstate = this.getBlockState();
            boolean flag = blockstate.get(SafeBlock.OPEN);
            if (!flag) {
                //this.playSound(blockstate, SoundEvents.BLOCK_BARREL_OPEN);
                this.world.playSound((PlayerEntity)null, this.pos.getX()+0.5, this.pos.getY()+0.5, this.pos.getZ()+0.5,
                        SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.7F);
                this.world.setBlockState(this.getPos(), blockstate.with(SafeBlock.OPEN, true), 3);
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

            boolean flag = blockstate.get(SackBlock.OPEN);
            if (flag) {
                this.world.playSound(null, this.pos.getX()+0.5, this.pos.getY()+0.5, this.pos.getZ()+0.5,
                        SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.7F);
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

    //TODO: make jars use blockentity tag like here. here works fine
    public void loadFromNbt(CompoundNBT compound) {
        this.items = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(compound) && compound.contains("Items", 9)) {
            ItemStackHelper.loadAllItems(compound, this.items);
        }
        if(compound.contains("Owner"))
            this.owner=compound.getUniqueId("Owner");
        if(compound.contains("OwnerName"))
            this.ownerName=compound.getString("OwnerName");
    }

    public CompoundNBT saveToNbt(CompoundNBT compound) {
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.items, false);
        }
        if(this.owner!=null)
            compound.putUniqueId("Owner",this.owner);
        if(this.ownerName!=null)
            compound.putString("OwnerName",this.ownerName);
        return compound;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.items = itemsIn;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return new ShulkerBoxContainer(id, player, this);
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
        if((Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock)||
                (stack.getItem() instanceof SackItem))return false;
        return super.isItemValidForSlot(index,stack);
    }


}
