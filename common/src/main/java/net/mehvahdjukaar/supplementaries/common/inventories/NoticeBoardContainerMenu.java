package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.moonlight.api.misc.IContainerProvider;
import net.mehvahdjukaar.supplementaries.common.block.tiles.NoticeBoardBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModMenuTypes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


public class NoticeBoardContainerMenu extends AbstractContainerMenu implements IContainerProvider {
    public final NoticeBoardBlockTile container;


    public NoticeBoardContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, ModRegistry.NOTICE_BOARD_TILE.get().getBlockEntity(playerInventory.player.level(),
                packetBuffer.readBlockPos()));
    }

    public NoticeBoardContainerMenu(int id, Inventory playerInventory, NoticeBoardBlockTile container) {

        super(ModMenuTypes.NOTICE_BOARD.get(), id);
        //tile inventory
        this.container = container;
        checkContainerSize(container, 1);
        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, 0, 35, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return (CommonConfigs.Building.NOTICE_BOARDS_UNRESTRICTED.get() || NoticeBoardBlockTile.isPageItem(stack.getItem()));
            }
        });


        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInventory, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInventory, si, 8 + si * 18, 142));
    }

    @Override
    public NoticeBoardBlockTile getContainer() {
        return container;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.container.stillValid(playerIn);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemCopy = item.copy();
            if (index < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(item, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.moveItemStackTo(item, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (item.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (item.getCount() == itemCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, item);
        }

        return itemCopy;
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.container.stopOpen(playerIn);
    }

}