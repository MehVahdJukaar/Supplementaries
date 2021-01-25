package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.block.util.IMapDisplay;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.client.gui.NoticeBoardContainer;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;


public class NoticeBoardBlockTile extends LockableLootTileEntity implements INameable, ISidedInventory, IMapDisplay {
    private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
    private String txt = null;
    private int fontScale = 1;
    private DyeColor textColor = DyeColor.BLACK;
    private List<IReorderingProcessor> cachedPageLines = Collections.emptyList();
    //used to tell renderer when it has to slit new line(have to do it there cause i need fontrenderer function)
    private boolean inventoryChanged = true;
    // private int packedFrontLight =0;
    public boolean textVisible = true; //for culling
    private ITextComponent customName;

    public NoticeBoardBlockTile() {
        super(Registry.NOTICE_BOARD_TILE.get());
    }

    @Override
    public void setCustomName(ITextComponent name) {
        this.customName = name;
    }

    @Override
    public ITextComponent getName() {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    @Override
    public ITextComponent getCustomName() {
        return this.customName;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.notice_board");
    }

    //update blockstate and plays sound
    public void updateBoardBlock(boolean b) {

        BlockState _bs = this.world.getBlockState(this.pos);
        if(_bs.get(BlockStateProperties.HAS_BOOK)!=b){
            this.world.setBlockState(this.pos, _bs.with(BlockStateProperties.HAS_BOOK,b), 2);
            if(b){
                this.world.playSound(null, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1F,
                        this.world.rand.nextFloat() * 0.10F + 0.85F);
            }
            else{
                this.world.playSound(null, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1F,
                        this.world.rand.nextFloat() * 0.10F + 0.50F);
            }
        }
    }

    //hijacking this method to work with hoppers
    @Override
    public void markDirty() {
        this.updateTile();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        //this.updateServerAndClient();
        super.markDirty();
    }

    @Override
    public ItemStack getMapStack(){
        return this.getStackInSlot(0);
    }

    public void updateTile() {
        //updateTextVisibility();
        if(this.world != null && !this.world.isRemote()){
            ItemStack itemstack = getStackInSlot(0);
            Item item = itemstack.getItem();
            String s = null;
            this.inventoryChanged = true;
            this.cachedPageLines = Collections.emptyList();

            if (item instanceof  WrittenBookItem) {
                CompoundNBT com = itemstack.getTag();
                if(WrittenBookItem.validBookTagContents(com)){

                    ListNBT listnbt = com.getList("pages", 8).copy();
                    s = listnbt.getString(0);
                }
            }
            else if(item instanceof  WritableBookItem){
                CompoundNBT com = itemstack.getTag();
                if(WritableBookItem.isNBTValid(com)){

                    ListNBT listnbt = com.getList("pages", 8).copy();
                    s = listnbt.getString(0);
                }
            }


            if (s != null) {
                //this.inventoryChanged = true;
                this.txt = s;
                this.updateBoardBlock(true);
            }
            else {
                this.txt = null;
                this.updateBoardBlock(false);
            }
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        if (compound.contains("CustomName", 8)) {
            this.customName = ITextComponent.Serializer.getComponentFromJson(compound.getString("CustomName"));
        }
        if (!this.checkLootAndRead(compound)) {
            this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        }
        ItemStackHelper.loadAllItems(compound, this.stacks);
        this.txt = compound.getString("Text");
        this.fontScale = compound.getInt("FontScale");
        //TODO: rework this
        this.inventoryChanged = compound.getBoolean("invchanged");
        this.textColor = DyeColor.byTranslationKey(compound.getString("Color"), DyeColor.BLACK);
        this.textVisible = compound.getBoolean("TextVisible");


        // this.packedFrontLight = compound.getInt("light");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        if (this.customName != null) {
            compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.stacks);
        }
        if (this.txt != null) {
            compound.putString("Text", this.txt);
        }
        compound.putInt("FontScale", this.fontScale);
        compound.putBoolean("invchanged", this.inventoryChanged);
        compound.putString("Color", this.textColor.getTranslationKey());
        compound.putBoolean("TextVisible", this.textVisible);

        // compound.putInt("light", this.packedFrontLight);

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
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return new NoticeBoardContainer(id, player,this);
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
        if(!stack.isEmpty()&&this.isEmpty()&&ServerConfigs.cached.NOTICE_BOARDS_UNRESTRICTED)return true;
        return (this.isEmpty()&&((ItemTags.LECTERN_BOOKS!=null&&stack.getItem().isIn(ItemTags.LECTERN_BOOKS))|| stack.getItem() instanceof FilledMapItem));
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

    public DyeColor getTextColor() {
        return this.textColor;
    }

    public boolean setTextColor(DyeColor newColor) {
        if (newColor != this.getTextColor()) {
            this.textColor = newColor;
            //this.world.notifyBlockUpdate(this.getPos(), this.getBlockState(), this.getBlockState(), 3);
            return true;
        } else {
            return false;
        }
    }

    //do this inside here
    public void setFontScale(int s) {
        this.fontScale = s;
    }

    public void setChachedPageLines(List<IReorderingProcessor> l) {
        this.cachedPageLines = l;
    }

    public List<IReorderingProcessor> getCachedPageLines() {
        return this.cachedPageLines;
    }

    public int getFontScale() {
        return this.fontScale;
    }

    public boolean getFlag() {
        if (this.inventoryChanged) {
            this.inventoryChanged = false;
            return true;
        }
        return false;
    }

    public Direction getDirection(){
        return this.getBlockState().get(NoticeBoardBlock.FACING);
    }

    public float getYaw() {
        return -this.getDirection().getHorizontalAngle();
    }

    public boolean getAxis() {
        Direction d = this.getDirection();
        return d == Direction.NORTH || d == Direction.SOUTH;
    }

    public int getFrontLight() {
        return WorldRenderer.getCombinedLight(this.world, this.pos.offset(this.getDirection()));
    }

    public String getText() {
        if (this.txt != null) {
            return this.txt;
        } else {
            return "";
        }
    }
}