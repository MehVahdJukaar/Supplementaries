package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.moonlight.api.misc.IContainerProvider;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonUtils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;


public class CannonContainerMenu extends AbstractContainerMenu implements IContainerProvider {

    public final CannonBlockTile cannon;

    @Override
    public CannonBlockTile getContainer() {
        return cannon;
    }

    //client container factory
    public CannonContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(ModMenuTypes.CANNON.get(), id, playerInventory, CannonUtils.cannonFromNetwork(playerInventory.player.level(), TileOrEntityTarget.read(packetBuffer)));
    }

    public <T extends CannonContainerMenu> CannonContainerMenu(int id, Inventory playerInventory, CannonBlockTile cannon) {
        this(ModMenuTypes.CANNON.get(), id, playerInventory, cannon);
    }

    public <T extends CannonContainerMenu> CannonContainerMenu(MenuType<T> type, int id, Inventory playerInventory, CannonBlockTile cannon) {
        super(type, id);

        //tile inventory
        this.cannon = cannon;

        CannonBlockTile inventory = this.getContainer();
        checkContainerSize(inventory, 2);
        inventory.startOpen(playerInventory.player);

        this.addSlot(new DelegatingSlot(inventory, 0, 38, 35, this));
        this.addSlot(new DelegatingSlot(inventory, 1, 85, 35, this));

        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInventory, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInventory, si, 8 + si * 18, 142));
    }


    @Override
    public boolean stillValid(Player playerIn) {
        return this.cannon.stillValid(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemCopy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack item = slot.getItem();
            itemCopy = item.copy();
            CannonBlockTile container = this.getContainer();
            if (index < container.getContainerSize()) {
                if (!this.moveItemStackTo(item, container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.moveItemStackTo(item, 0, container.getContainerSize(), false)) {
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

    @Override
    public void removed(@NotNull Player playerIn) {
        super.removed(playerIn);
        this.getContainer().stopOpen(playerIn);
    }

}