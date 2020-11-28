package net.mehvahdjukaar.supplementaries.blocks;

import io.netty.buffer.Unpooled;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.gui.NoticeBoardContainer;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.lighting.IWorldLightListener;
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


public class NoticeBoardBlockTile extends LockableLootTileEntity implements INameable, ISidedInventory{
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
        super(Registry.NOTICE_BOARD_TILE);
    }

    public void setCustomName(ITextComponent name) {
        this.customName = name;
    }

    public ITextComponent getName() {
        return this.customName != null ? this.customName : this.getDefaultName();
    }

    public ITextComponent getCustomName() {
        return this.customName;
    }

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

    public void updateTextVisibility(BlockState state,World world, BlockPos pos, BlockPos fromPos){
        Direction dir = state.get(NoticeBoardBlock.FACING);
        if(fromPos.toLong()==pos.offset(dir).toLong()){
            BlockState frontstate = world.getBlockState(fromPos);

            this.textVisible = !frontstate.isSolidSide(world, fromPos, dir.getOpposite());
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
        }
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
        this.txt = compound.getString("txt");
        this.fontScale = compound.getInt("fontscale");
        this.inventoryChanged = compound.getBoolean("invchanged");
        this.textColor = DyeColor.byTranslationKey(compound.getString("color"), DyeColor.BLACK);
        this.textVisible = compound.getBoolean("textvisible");
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
            compound.putString("txt", this.txt);
        }
        compound.putInt("fontscale", this.fontScale);
        compound.putBoolean("invchanged", this.inventoryChanged);
        compound.putString("color", this.textColor.getTranslationKey());
        compound.putBoolean("textvisible", this.textVisible);
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
        return new NoticeBoardContainer(id, player, new PacketBuffer(Unpooled.buffer()).writeBlockPos(this.getPos()));
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
        if(!stack.isEmpty()&&this.isEmpty()&&ServerConfigs.cached.NOTICE_BOARDS_UNRESTRICTED)return true;
        return (this.isEmpty()&&(stack.getItem() == Items.WRITTEN_BOOK || stack.getItem() == Items.WRITABLE_BOOK || stack.getItem() instanceof FilledMapItem));
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
        World world = this.getWorld();
        assert world != null;
        IWorldLightListener block = world.getLightManager().getLightEngine(LightType.BLOCK);
        IWorldLightListener sky = world.getLightManager().getLightEngine(LightType.SKY);
        BlockPos newpos = this.pos.offset(this.getDirection());
        int u = block.getLightFor(newpos) * 16;
        int v = sky.getLightFor(newpos) * 16;
        return ((v << 16) | u);
        // return this.packedFrontLight;
    }

    public String getText() {
        if (this.txt != null) {
            return this.txt;
        } else {
            return "";
        }
    }
}