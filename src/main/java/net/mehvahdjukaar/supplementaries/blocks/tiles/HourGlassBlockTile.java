package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.CommonUtil.HourGlassSandType;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.stream.IntStream;

public class HourGlassBlockTile extends LockableLootTileEntity implements ISidedInventory, ITickableTileEntity {
    private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
    private static final HashMap<Item,Float> sandsTimesMap = new HashMap<>();
    public HourGlassSandType sandType = HourGlassSandType.DEFAULT;
    public float progress = 0; //0-1 percentage of progress
    public float prevProgress = 0;
    public int power = 0;
    public HourGlassBlockTile() {
        super(Registry.HOURGLASS_TILE);
        //TODO: add configs
        sandsTimesMap.put(Items.SAND,0.1f);
    }

    //hijacking this method to work with hoppers
    @Override
    public void markDirty() {
        //this.updateServerAndClient();
        this.updateTile();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        super.markDirty();
    }

    public void updateTile(){
        this.sandType = HourGlassSandType.getHourGlassSandType(this.getStackInSlot(0).getItem());
        int p = this.getDirection()==Direction.DOWN?1:0;
        int l = this.sandType.getLight();
        if(l!=this.getBlockState().get(HourGlassBlock.LIGHT_LEVEL)){
            world.setBlockState(this.pos, this.getBlockState().with(HourGlassBlock.LIGHT_LEVEL,l),4);
        }
        this.prevProgress=p;
        this.progress=p;
    }

    @Override
    public void tick() {
        Direction dir = this.getDirection();
        if(!this.sandType.isEmpty()){
            this.prevProgress = this.progress;
            if(dir==Direction.UP && this.progress != 1){
                this.progress = Math.min(this.progress + this.sandType.increment, 1f);
            }
            else if(dir==Direction.DOWN && this.progress != 0){
                this.progress = Math.max(this.progress - this.sandType.increment, 0f);
            }
        }

        if(!this.world.isRemote){
            int p;
            if(dir==Direction.DOWN) {
                p = (int) ((1-this.progress) * 15f);
            }
            else{
                p = (int) ((this.progress) * 15f);
            }
            if(p!=this.power){
                this.power=p;
                this.world.updateComparatorOutputLevel(this.pos,this.getBlockState().getBlock());
            }
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if (!this.checkLootAndRead(compound)) {
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        }
        ItemStackHelper.loadAllItems(compound, this.stacks);
        this.sandType = CommonUtil.HourGlassSandType.values()[compound.getInt("sand_type")];
        this.progress = compound.getFloat("progress");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.stacks);
        }
        compound.putInt("sand_type", this.sandType.ordinal());
        compound.putFloat("progress", this.progress);
        return compound;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(this.getBlockState(),pkt.getNbtCompound());
    }

    @Override
    public int getSizeInventory() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new StringTextComponent("hourglass");
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }


    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return ChestContainer.createGeneric9X3(id, player, this);
    }


    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    public void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return this.isEmpty() && !CommonUtil.HourGlassSandType.getHourGlassSandType(stack.getItem()).isEmpty();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getSizeInventory()).toArray();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, @Nullable Direction direction) {
        if(direction==Direction.UP) {
            return this.isItemValidForSlot(0,stack);
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        Direction dir = this.getBlockState().get(HourGlassBlock.FACING);
        return (dir==Direction.UP && this.progress==1)||(dir==Direction.DOWN && this.progress==0);
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

    public Direction getDirection(){
        return this.getBlockState().get(HourGlassBlock.FACING);
    }

}

