package net.mehvahdjukaar.supplementaries.gui;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;


public class SackContainer extends Container  {
    public final IInventory inventory;

    public SackContainer(int id, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
        this(id,playerInventory);
    }

    public SackContainer(int id, PlayerInventory playerInventory) {
        this(id, playerInventory, new Inventory(9));
    }

    public SackContainer(int id, PlayerInventory playerInventory, IInventory inventory) {

        super(Registry.SACK_CONTAINER, id);
        //tile inventory
        this.inventory = inventory;
        assertInventorySize(inventory, 9);
        inventory.openInventory(playerInventory.player);

        int add = ServerConfigs.cached.SACK_SLOTS;
        int xp = 44-(add*18);
        for(int j = 0; j < (5+(add*2)); ++j) {
            this.addSlot(new SackSlot(inventory, j, xp + j * 18, 35));
        }


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
            int activeSlots = (5+(ServerConfigs.cached.SACK_SLOTS*2));
            if (index < activeSlots) {
                if (!this.mergeItemStack(itemstack1, activeSlots, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, activeSlots, false)) {
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


