package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.supplementaries.block.blocks.SackBlock;
import net.mehvahdjukaar.supplementaries.block.blocks.SafeBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.stream.IntStream;

public class SafeBlockTile extends RandomizableContainerBlockEntity implements WorldlyContainer, IOwnerProtected {

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int numPlayersUsing;

    public String password = null;
    public String ownerName = null;
    public UUID owner = null;

    public SafeBlockTile() {
        super(ModRegistry.SAFE_TILE.get());
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    public boolean canPlayerOpen(Player player, boolean feedbackMessage) {
        if (player.isCreative()) return true;
        if (ServerConfigs.cached.SAFE_SIMPLE) {
            if (this.isNotOwnedBy(player)) {
                if (feedbackMessage)
                    player.displayClientMessage(new TranslatableComponent("message.supplementaries.safe.owner", this.ownerName), true);
                return false;
            }
        } else {
            return KeyLockableTile.doesPlayerHaveKeyToOpen(player, this.password, feedbackMessage, "safe");
        }
        return true;
    }

    @Nullable
    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.ownerName = level.getPlayerByUUID(owner).getName().getString();
        this.owner = owner;
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    public void clearOwner() {
        this.ownerName = null;
        this.owner = null;
        this.password = null;
    }

    @Override
    public Component getDisplayName() {
        if (ServerConfigs.cached.SAFE_SIMPLE) {
            if (this.ownerName != null) {
                return (new TranslatableComponent("gui.supplementaries.safe.name", this.ownerName, super.getDisplayName()));
            }
        } else if (this.password != null) {
            return (new TranslatableComponent("gui.supplementaries.safe.password", this.password, super.getDisplayName()));
        }
        return super.getDisplayName();
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.safe");
    }

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
            BlockState blockstate = this.getBlockState();
            boolean flag = blockstate.getValue(SafeBlock.OPEN);
            if (!flag) {
                //this.playSound(blockstate, SoundEvents.BLOCK_BARREL_OPEN);
                this.level.playSound(null, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5,
                        SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.65F);
                this.level.setBlock(this.getBlockPos(), blockstate.setValue(SafeBlock.OPEN, true), 3);
            }
            this.level.getBlockTicks().scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 5);
        }
    }

    public static int calculatePlayersUsing(Level world, BaseContainerBlockEntity tile, int x, int y, int z) {
        int i = 0;
        for (Player playerentity : world.getEntitiesOfClass(Player.class, new AABB((float) x - 5.0F, (float) y - 5.0F, (float) z - 5.0F, (float) (x + 1) + 5.0F, (float) (y + 1) + 5.0F, (float) (z + 1) + 5.0F))) {
            if (playerentity.containerMenu instanceof ShulkerBoxMenu) {
                //TODO: maybe make my own container instead of this hacky stuff?
                try {
                    for (Field f : ShulkerBoxMenu.class.getDeclaredFields())
                        if (Container.class.isAssignableFrom(f.getType())) {
                            f.setAccessible(true);
                            if (f.get(playerentity.containerMenu) == tile) {
                                ++i;
                            }
                        }
                } catch (Exception ignored) {
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

            boolean flag = blockstate.getValue(SackBlock.OPEN);
            if (flag) {
                this.level.playSound(null, this.worldPosition,
                        SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.65F);
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
        this.loadFromNbt(nbt);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        return this.saveToNbt(compound);
    }

    public void loadFromNbt(CompoundTag compound) {
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compound) && compound.contains("Items", 9)) {
            ContainerHelper.loadAllItems(compound, this.items);
        }
        this.loadOwner(compound);
        if (compound.contains("OwnerName"))
            this.ownerName = compound.getString("OwnerName");
        if (compound.contains("Password"))
            this.password = compound.getString("Password");
    }

    public CompoundTag saveToNbt(CompoundTag compound) {
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.items, false);
        }
        this.saveOwner(compound);
        if (this.ownerName != null)
            compound.putString("OwnerName", this.ownerName);
        if (this.password != null)
            compound.putString("Password", this.password);
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
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new ShulkerBoxMenu(id, player, this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveToNbt(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.loadFromNbt(pkt.getTag());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 0, this.getUpdateTag());
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return CommonUtil.isAllowedInShulker(stack);
    }

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