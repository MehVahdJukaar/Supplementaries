package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.block.util.IMapDisplay;
import net.mehvahdjukaar.supplementaries.block.util.ITextHolderProvider;
import net.mehvahdjukaar.supplementaries.block.util.TextHolder;
import net.mehvahdjukaar.supplementaries.client.gui.HangingSignGui;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


//TODO: make swaying tile an interface
public class HangingSignBlockTile extends SwayingBlockTile implements IMapDisplay, ITextHolderProvider, IOwnerProtected {
    public static final int MAX_LINES = 7;

    private UUID owner = null;

    public TextHolder textHolder;
    private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);

    static {
        maxSwingAngle = 45f;
        minSwingAngle = 2.5f;
        maxPeriod = 25f;
        angleDamping = 150f;
        periodDamping = 100f;
    }

    public HangingSignBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.HANGING_SIGN_TILE.get(), pos, state);
        this.textHolder = new TextHolder(MAX_LINES);
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Override
    public TextHolder getTextHolder() {
        return this.textHolder;
    }

    @Override
    public void openScreen(Level level, BlockPos pos, Player player) {
        HangingSignGui.open(this);
    }


    @Override
    public ItemStack getMapStack() {
        return this.getStackInSlot(0);
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        super.setChanged();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, this.stacks);

        this.textHolder.read(compound);
        this.loadOwner(compound);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.stacks);

        this.textHolder.write(tag);
        this.saveOwner(tag);
    }


    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
        return ContainerHelper.takeItem(this.getItems(), index);
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

