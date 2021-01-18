package net.mehvahdjukaar.supplementaries.blocks.tiles;

import net.mehvahdjukaar.supplementaries.common.IMapDisplay;
import net.mehvahdjukaar.supplementaries.common.ITextHolder;
import net.mehvahdjukaar.supplementaries.common.TextHolder;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;


public class HangingSignBlockTile extends SwayingBlockTile implements IMapDisplay, ITextHolder {
    public static final int MAXLINES = 5;

    public TextHolder textHolder;
    private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 2.5f;
        maxPeriod = 25f;
        angleDamping = 150f;
        periodDamping = 100f;
    }

    public HangingSignBlockTile() {
        super(Registry.HANGING_SIGN_TILE);
        this.textHolder = new TextHolder(MAXLINES);
    }

    @Override
    public TextHolder getTextHolder(){return this.textHolder;}

    @Override
    public ItemStack getMapStack(){
        return this.getStackInSlot(0);
    }

    @Override
    public void markDirty() {
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        super.markDirty();
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);

        this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.stacks);

        this.textHolder.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        ItemStackHelper.saveAllItems(compound, this.stacks);

        this.textHolder.write(compound);
        return compound;
    }

    //TODO: make this a ISidedInventory again
    public int getSizeInventory() {
        return stacks.size();
    }

    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    public void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.getItems(), index);
    }

    public ItemStack getStackInSlot(int index) {
        return this.getItems().get(index);
    }

    /*
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getSizeInventory()).toArray();
    }

    public int getInventoryStackLimit() {
        return 1;
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        this.getItems().set(index, stack);
        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    public void clear() {
        this.getItems().clear();
    }*/

}

