package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.supplementaries.reg.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;


@Deprecated(forRemoval = true)
public class SackContainerMenu extends VariableSizeContainerMenu {

    public SackContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super(ModMenuTypes.SACK.get(), id, playerInventory, packetBuffer);
    }

    public SackContainerMenu(int id, Inventory playerInventory, Container container, int unlockedSlots) {
        super(ModMenuTypes.SACK.get(),id, playerInventory, container, unlockedSlots);
    }

    public SackContainerMenu(int id, Inventory playerInventory, Container container) {
        super(ModMenuTypes.SACK.get(),id, playerInventory, container, container.getContainerSize());
    }
}


