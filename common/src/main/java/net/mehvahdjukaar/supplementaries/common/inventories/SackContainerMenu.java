package net.mehvahdjukaar.supplementaries.common.inventories;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;


public class SackContainerMenu extends VariableSizeContainerMenu {

    public SackContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super(id, playerInventory, packetBuffer);
    }

    public SackContainerMenu(int id, Inventory playerInventory, Container container, int unlockedSlots) {
        super(id, playerInventory, container, unlockedSlots);
    }

    public SackContainerMenu(int id, Inventory playerInventory, Container container) {
        super(id, playerInventory, container, container.getContainerSize());
    }
}


