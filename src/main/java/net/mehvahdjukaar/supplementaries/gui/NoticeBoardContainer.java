package net.mehvahdjukaar.supplementaries.gui;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;


public class NoticeBoardContainer extends Container  {
    public final IInventory inventory;

    public NoticeBoardContainer(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id,playerInventory);
    }

    public NoticeBoardContainer(int id, PlayerInventory playerInventory) {
        this(id, playerInventory, new Inventory(1));
    }

    public NoticeBoardContainer(int id, PlayerInventory playerInventory, IInventory inventory) {

        super(Registry.NOTICE_BOARD_CONTAINER, id);
        //tile inventory
        this.inventory = inventory;
        assertInventorySize(inventory, 1);
        inventory.openInventory(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 79, 39) {
            @Override
            public void onSlotChanged() {
                super.onSlotChanged();
                //NoticeBoardContainer.this.slotChanged(0, 0, 0);
            }
            @Override
            public boolean isItemValid(ItemStack stack) {
                if(!stack.isEmpty()&& ServerConfigs.cached.NOTICE_BOARDS_UNRESTRICTED)return true;
                return ((ItemTags.LECTERN_BOOKS!=null&&stack.getItem().isIn(ItemTags.LECTERN_BOOKS)) || stack.getItem() instanceof FilledMapItem);
            }
        });


        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInventory, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInventory, si, 8 + si * 18, 142));
    }



    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.inventory.isUsableByPlayer(playerIn);
    }
    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < this.inventory.getSizeInventory()) {
                if (!this.mergeItemStack(itemstack1, this.inventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, this.inventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.inventory.closeInventory(playerIn);
    }

}