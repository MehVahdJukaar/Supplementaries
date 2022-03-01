package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.supplementaries.common.block.IDynamicContainer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;


public class PresentContainerMenu extends AbstractContainerMenu implements IContainerProvider {

    protected final Container inventory;
    protected final BlockPos pos;

    @Override
    public Container getContainer() {
        return inventory;
    }

    //client container factory
    public PresentContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, null, packetBuffer.readBlockPos());
    }
    public PresentContainerMenu(int id, Inventory playerInventory, Container inventory, BlockPos pos) {
        this(ModRegistry.PRESENT_BLOCK_CONTAINER.get(),id, playerInventory, inventory,pos);
    }

    public <T extends PresentContainerMenu>PresentContainerMenu(MenuType<T> type, int id, Inventory playerInventory, Container inventory, BlockPos pos) {
        super(type, id);

        this.pos = pos;

        //tile inventory
        this.inventory = Objects.requireNonNullElseGet(inventory, () -> new SimpleContainer(1) {
            public void setChanged() {
                super.setChanged();
                PresentContainerMenu.this.slotsChanged(this);
            }
        });

        checkContainerSize(this.inventory, 1);
        this.inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(this.inventory, 0, getSlotX(), getSlotY()) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PresentBlockTile.isAcceptableItem(stack);
            }
        });

        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInventory, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInventory, si, 8 + si * 18, 142));
    }

    protected int getSlotY() {
        return 20;
    }

    protected int getSlotX(){
        return 17;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.inventory.stillValid(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemstack = item.copy();
            if (index < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(item, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(item, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (item.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.inventory.stopOpen(playerIn);
        if (playerIn.level.getBlockEntity(this.pos) instanceof IDynamicContainer tile && !tile.canHoldItems()) {
            this.clearContainer(playerIn, this.inventory);
        }

    }

}