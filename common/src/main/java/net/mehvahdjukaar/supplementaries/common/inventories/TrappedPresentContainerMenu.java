package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.supplementaries.common.block.tiles.AbstractPresentBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModMenuTypes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

import static net.mehvahdjukaar.supplementaries.common.inventories.NoticeBoardContainerMenu.getBlockEntityOrThrow;


public class TrappedPresentContainerMenu extends PresentContainerMenu {

    public TrappedPresentContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, getBlockEntityOrThrow(
                TileOrEntityTarget.read(packetBuffer), playerInventory.player.level(),
                ModRegistry.TRAPPED_PRESENT_TILE.get()));
    }

    public TrappedPresentContainerMenu(int id, Inventory playerInventory, AbstractPresentBlockTile inventory) {
        super(ModMenuTypes.TRAPPED_PRESENT_BLOCK.get(), id, playerInventory, inventory);
    }

    @Override
    protected int getSlotX() {
        return 17 + 45;
    }

    protected int getSlotY() {
        return 36;
    }

}