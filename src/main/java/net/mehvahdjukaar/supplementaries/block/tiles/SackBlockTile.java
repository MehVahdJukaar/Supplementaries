package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.SackBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.inventories.SackContainer;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import vazkii.quark.api.ITransferManager;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class SackBlockTile extends RandomizableContainerBlockEntity implements WorldlyContainer, ICapabilityProvider, ITransferManager {

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int numPlayersUsing;

    public SackBlockTile() {
        super(ModRegistry.SACK_TILE.get());
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }


    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.sack");
    }

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
            BlockState blockstate = this.getBlockState();
            boolean flag = blockstate.getValue(SackBlock.OPEN);
            if (!flag) {
                this.level.playSound(null, this.worldPosition,
                        SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.55F);
                this.level.playSound(null, this.worldPosition,
                        SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.7F);
                this.level.setBlock(this.getBlockPos(), blockstate.setValue(SackBlock.OPEN, true), 3);
            }
            this.level.getBlockTicks().scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 5);
        }
    }
    public static int calculatePlayersUsing(Level world, BaseContainerBlockEntity tile, int x, int y, int z) {
        int i = 0;
        for(Player playerentity : world.getEntitiesOfClass(Player.class, new AABB((float)x - 5.0F, (float)y - 5.0F, (float)z - 5.0F, (float)(x + 1) + 5.0F, (float)(y + 1) + 5.0F, (float)(z + 1) + 5.0F))) {
            if (playerentity.containerMenu instanceof SackContainer) {
                Container iinventory = ((SackContainer)playerentity.containerMenu).inventory;
                if (iinventory == tile) {
                    ++i;
                }
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
            /*
            if (!blockstate.isIn(Blocks.BARREL)) {
                this.remove();
                return;
            }*/

            boolean flag = blockstate.getValue(SackBlock.OPEN);
            if (flag) {
                //this.playSound(blockstate, SoundEvents.BLOCK_BARREL_CLOSE);
                this.level.playSound((Player)null, this.worldPosition.getX()+0.5, this.worldPosition.getY()+0.5, this.worldPosition.getZ()+0.5,
                        SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.5F);
                this.level.playSound(null, this.worldPosition.getX()+0.5, this.worldPosition.getY()+0.5, this.worldPosition.getZ()+0.5,
                        SoundEvents.LEASH_KNOT_PLACE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.6F);
                this.level.setBlock(this.getBlockPos(), blockstate.setValue(SackBlock.OPEN, false), 3);
            }
        }

    }

    @Override
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
        }
    }

    @Override
    public void load(BlockState state, CompoundTag nbt) {
        super.load(state, nbt);
        this.loadFromTag(nbt);
    }

    //TODO: separate save to nbt from write so you don't write data you don't need. it update packet too
    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        return this.saveToTag(compound);
    }

    public void loadFromTag(CompoundTag compoundNBT) {
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compoundNBT) && compoundNBT.contains("Items", 9)) {
            ContainerHelper.loadAllItems(compoundNBT, this.items);
        }
    }

    public CompoundTag saveToTag(CompoundTag compoundNBT) {
        if (!this.trySaveLootTable(compoundNBT)) {
            ContainerHelper.saveAllItems(compoundNBT, this.items, false);
        }

        return compoundNBT;
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
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new SackContainer(id, player, this);
    }

    public int getUnlockedSlots(){
        return ServerConfigs.cached.SACK_SLOTS;
    }

    public boolean isSlotUnlocked(int ind){
        return ind < this.getUnlockedSlots();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return isSlotUnlocked(index) && CommonUtil.isAllowedInShulker(stack);
    }

    //TODO: figure out what this handlers and ISided inventory do
    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return isSlotUnlocked(index);
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

    @Override
    public boolean acceptsTransfer(Player player) {
        return true;
    }
}
