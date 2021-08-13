package net.mehvahdjukaar.supplementaries.block.tiles;


import net.mehvahdjukaar.supplementaries.block.blocks.PresentBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.inventories.PresentContainer;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class PresentBlockTile extends LockableLootTileEntity implements ISidedInventory, ICapabilityProvider {

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int numPlayersUsing;

    private String recipient = null;
    private String sender = null;
    private boolean packed = false;


    public PresentBlockTile() {
        super(null);
        //super(Registry.PRESENT_TILE.get());
    }

    public boolean isUnused(){
        return this.numPlayersUsing<=0;
    }

    public static boolean isPacked(ItemStack stack){
        CompoundNBT com = stack.getTag();
        if(com!=null){
            CompoundNBT nbt = com.getCompound("BlockEntityTag");
            if(nbt!=null){
                return nbt.getBoolean("Packed");
            }
        }
        return false;
    }

    public boolean isPacked(){
        return this.packed;
    }

    public void unpack(){
        this.recipient = null;
        this.sender = null;
        this.packed = false;
        if(!this.level.isClientSide)
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(PresentBlock.OPEN, true), 3);
    }

    public void pack(String recipient, String sender, boolean doPack){
        this.recipient = recipient;
        this.sender = sender;
        this.packed = doPack;
        if(doPack && !this.level.isClientSide)
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(PresentBlock.OPEN, false), 3);
    }
    public void pack(String recipient, String sender){
        this.pack(recipient,sender,true);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.present");
    }

    @Override
    public void startOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
        }
    }

    @Override
    public void stopOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        this.loadFromTag(nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        return this.saveToTag(compound);
    }

    public void loadFromTag(CompoundNBT tag) {
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag) && tag.contains("Items", 9)) {
            ItemStackHelper.loadAllItems(tag, this.items);
        }
        if(tag.contains("Recipient"))
            this.recipient = tag.getString("Recipient");
        if(tag.contains("Sender"))
            this.sender = tag.getString("Sender");
        this.packed = tag.getBoolean("Packed");
    }

    public CompoundNBT saveToTag(CompoundNBT tag) {
        if (!this.trySaveLootTable(tag)) {
            ItemStackHelper.saveAllItems(tag, this.items, false);
        }
        if(this.recipient!=null)
            tag.putString("Recipient",this.recipient);
        if(this.sender!=null)
            tag.putString("Sender",this.sender);

        tag.putBoolean("Packed",this.packed);
        return tag;
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
        return new PresentContainer(id, player, this,this.worldPosition);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return CommonUtil.isAllowedInShulker(stack);
    }

    //TODO: figure out what this handlers and ISided inventory do
    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return handlers[facing.ordinal()].cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
    }
}
