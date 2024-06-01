package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.moonlight.api.misc.IContainerProvider;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModMenuTypes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;

public class SafeContainerMenu extends ShulkerBoxMenu implements IContainerProvider {

    private final SafeBlockTile tile;

    public SafeContainerMenu(int id, Inventory inventory, SafeBlockTile container) {
        super(id, inventory, container);
        this.tile = container;
    }

    public SafeContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, ModRegistry.SAFE_TILE.get().getBlockEntity(
                playerInventory.player.level(), packetBuffer.readBlockPos()
        ));
    }

    @Override
    public MenuType<?> getType() {
        return ModMenuTypes.SAFE.get();
    }

    @Override
    protected Slot addSlot(Slot slot) {
        if (slot instanceof ShulkerBoxSlot) {
            return super.addSlot(new DelegatingSlot(container, slot.getContainerSlot(), slot.x, slot.y, this));
        } else {
            return super.addSlot(slot);
        }
    }

    @Override
    public Container getContainer() {
        return tile;
    }
}