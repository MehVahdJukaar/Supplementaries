package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PulleyBlockTile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


public class PulleyBlockContainerMenu extends AbstractContainerMenu implements IContainerProvider {
    public final Container inventory;

    @Override
    public Container getContainer() {
        return inventory;
    }

    public PulleyBlockContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id,playerInventory);
    }

    public PulleyBlockContainerMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(1));
    }

    public PulleyBlockContainerMenu(int id, Inventory playerInventory, Container inventory) {

        super(ModRegistry.PULLEY_BLOCK_CONTAINER.get(), id);
        //tile inventory
        this.inventory = inventory;
        checkContainerSize(inventory, 1);
        inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 79, 39) {
            @Override
            public void setChanged() {
                super.setChanged();
                //NoticeBoardContainer.this.slotChanged(0, 0, 0);
            }
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PulleyBlockTile.getContentType(stack.getItem())!=BlockProperties.Winding.NONE;
            }
        });


        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInventory, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInventory, si, 8 + si * 18, 142));
    }



    @Override
    public boolean stillValid(Player playerIn) {
        return this.inventory.stillValid(playerIn);
    }
    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(itemstack1, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    /**
     * Called when the container is closed.
     */
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.inventory.stopOpen(playerIn);
    }

}