package net.mehvahdjukaar.supplementaries.common.inventories;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.AbstractPresentBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class TrappedPresentContainerMenu extends PresentContainerMenu {

    public TrappedPresentContainerMenu(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory, (AbstractPresentBlockTile) playerInventory.player.level()
                .getBlockEntity(packetBuffer.readBlockPos()));
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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if(PlatHelper.getPlatform().isFabric())return ItemStack.EMPTY; //bandaid for a mc bug thats fixed in 1.21 and by forge
        return super.quickMoveStack(player, index);
    }
}