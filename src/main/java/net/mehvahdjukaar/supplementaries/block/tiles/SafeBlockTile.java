package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.SackBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.SafeBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
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
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.stream.IntStream;

public class SafeBlockTile extends LockableLootTileEntity implements ISidedInventory{

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int numPlayersUsing;

    public String password = null;
    public String ownerName = null;
    public UUID owner = null;

    public SafeBlockTile() {
        super(Registry.SAFE_TILE.get());
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    public void setOwner(UUID owner){
        this.ownerName=level.getPlayerByUUID(owner).getName().getString();
        this.owner=owner;
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public void clearOwner(){
        this.ownerName=null;
        this.owner=null;
        this.password=null;
    }

    public boolean isOwnedBy(PlayerEntity player){
        return (this.owner!=null && this.owner.equals(player.getUUID()));
    }
    //owner==null is public
    public boolean isNotOwnedBy(PlayerEntity player){
        return (this.owner!=null && !this.owner.equals(player.getUUID()));
    }


    @Override
    public ITextComponent getDisplayName() {
        if(ServerConfigs.cached.SAFE_SIMPLE) {
            if (this.ownerName != null) {
                return (new TranslationTextComponent("gui.supplementaries.safe.name", this.ownerName, super.getDisplayName()));
            }
        }
        else if(this.password != null){
            return (new TranslationTextComponent("gui.supplementaries.safe.password", this.password, super.getDisplayName()));
        }
        return super.getDisplayName();
    }

    @Override
    protected ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.safe");
    }

    @Override
    public void startOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
            BlockState blockstate = this.getBlockState();
            boolean flag = blockstate.getValue(SafeBlock.OPEN);
            if (!flag) {
                //this.playSound(blockstate, SoundEvents.BLOCK_BARREL_OPEN);
                this.level.playSound(null, this.worldPosition.getX()+0.5, this.worldPosition.getY()+0.5, this.worldPosition.getZ()+0.5,
                        SoundEvents.IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.65F);
                this.level.setBlock(this.getBlockPos(), blockstate.setValue(SafeBlock.OPEN, true), 3);
            }
            this.level.getBlockTicks().scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 5);
        }
    }

    public static int calculatePlayersUsing(World world, LockableTileEntity tile, int x, int y, int z) {
        int i = 0;
        for(PlayerEntity playerentity : world.getEntitiesOfClass(PlayerEntity.class, new AxisAlignedBB((float)x - 5.0F, (float)y - 5.0F, (float)z - 5.0F, (float)(x + 1) + 5.0F, (float)(y + 1) + 5.0F, (float)(z + 1) + 5.0F))) {
            if (playerentity.containerMenu instanceof ShulkerBoxContainer) {
                //TODO: maybe make my own container instead of this hacky stuff?
                try {
                    for (Field f : ShulkerBoxContainer.class.getDeclaredFields())
                        if(IInventory.class.isAssignableFrom(f.getType())){
                            f.setAccessible(true);
                            if(f.get(playerentity.containerMenu) == tile){
                                ++i;
                            }
                        }
                }catch (Exception ignored) {}
            }
        }
        return i;
    }

    public void barrelTick() {
        int i = this.worldPosition.getX();
        int j = this.worldPosition.getY();
        int k = this.worldPosition.getZ();
        this.numPlayersUsing = calculatePlayersUsing(this.level, this, i, j, k);
        if (this.numPlayersUsing > 0) {
            this.level.getBlockTicks().scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 5);
        } else {
            BlockState blockstate = this.getBlockState();

            boolean flag = blockstate.getValue(SackBlock.OPEN);
            if (flag) {
                this.level.playSound(null, this.worldPosition,
                        SoundEvents.IRON_TRAPDOOR_CLOSE, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.65F);
                this.level.setBlock(this.getBlockPos(), blockstate.setValue(SackBlock.OPEN, false), 3);
            }
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
        this.loadFromNbt(nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        return this.saveToNbt(compound);
    }

    //TODO: make jars use blockentity tag like here. here works fine
    public void loadFromNbt(CompoundNBT compound) {
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compound) && compound.contains("Items", 9)) {
            ItemStackHelper.loadAllItems(compound, this.items);
        }
        if(compound.contains("Owner"))
            this.owner=compound.getUUID("Owner");
        if(compound.contains("OwnerName"))
            this.ownerName=compound.getString("OwnerName");
        if(compound.contains("Password"))
            this.password=compound.getString("Password");
    }

    public CompoundNBT saveToNbt(CompoundNBT compound) {
        if (!this.trySaveLootTable(compound)) {
            ItemStackHelper.saveAllItems(compound, this.items, false);
        }
        if(this.owner!=null)
            compound.putUUID("Owner",this.owner);
        if(this.ownerName!=null)
            compound.putString("OwnerName",this.ownerName);
        if(this.password!=null)
            compound.putString("Password",this.password);
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
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return CommonUtil.isAllowedInShulker(stack);
    }


    //TODO: FIX this so it can only put from top
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