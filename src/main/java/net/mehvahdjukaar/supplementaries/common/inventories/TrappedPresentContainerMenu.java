package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;


public class TrappedPresentContainerMenu extends PresentContainerMenu {

    public TrappedPresentContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        super(id, playerInventory, null, packetBuffer.readBlockPos());
    }

    public TrappedPresentContainerMenu(int id, Inventory playerInventory, Container inventory, BlockPos pos) {
        super(ModRegistry.TRAPPED_PRESENT_BLOCK_CONTAINER.get(), id, playerInventory, inventory, pos);
    }

    @Override
    protected int getSlotX() {
        return 17 + 45;
    }

    protected int getSlotY() {
        return 36;
    }
}