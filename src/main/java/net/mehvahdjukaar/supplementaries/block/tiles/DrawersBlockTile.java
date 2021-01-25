package net.mehvahdjukaar.supplementaries.block.tiles;

/*
public class DrawersBlockTile extends LockableLootTileEntity implements ISidedInventory, ITickableTileEntity {
    private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(14, ItemStack.EMPTY);
    public float opening = 0; //from 0 to 1
    public float prevOpening = 0;

    public DrawersBlockTile() {
        super(Registry.DRAWERS_TILE);
    }

    @Override
    public void tick(){

            this.prevOpening=this.opening;
            if(this.opening>0){
                this.opening=(float) Math.max(0, this.opening-0.01);

            }

    }

    public Direction getDirection(){
        return this.getBlockState().get(DrawersBlock.FACING);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if (!this.checkLootAndRead(compound)) {
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        }
        ItemStackHelper.loadAllItems(compound, this.stacks);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.stacks);
        }
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
        this.read(this.getBlockState(), pkt.getNbtCompound());
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
        return new StringTextComponent("drawers");
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return ChestContainer.createGeneric9X3(id, player, this);
        //return new DrawerGuiGui.GuiContainerMod(id, player, new PacketBuffer(Unpooled.buffer()).writeBlockPos(this.getPos()));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Drawers");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

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

*/