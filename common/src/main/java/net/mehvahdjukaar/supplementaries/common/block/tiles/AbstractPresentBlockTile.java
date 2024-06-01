package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.block.IWeakContainer;
import net.mehvahdjukaar.supplementaries.common.inventories.TrappedPresentContainerMenu;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPresentBlockTile extends OpeneableContainerBlockEntity implements IWeakContainer {

    protected AbstractPresentBlockTile(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state, 1);
    }

    public abstract InteractionResult interact(Level level, BlockPos pos, BlockState state, Player player);

    @Override
    protected void updateBlockState(BlockState state, boolean b) {
    }

    @Override
    protected void playOpenSound(BlockState state) {
    }

    @Override
    protected void playCloseSound(BlockState state) {
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv) {
        if (inv.player.isSpectator()) return null;
        return new TrappedPresentContainerMenu(id, inv, this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return MiscUtils.isAllowedInShulker(stack, level) && !(stack.getItem() instanceof PresentItem);
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    //sync stuff to client. Needed for pick block
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStack getPresentItem(ItemLike block) {
        CompoundTag compoundTag = new CompoundTag();
        this.saveAdditional(compoundTag);
        ItemStack itemstack = new ItemStack(block);
        if (!compoundTag.isEmpty()) {
            itemstack.addTagElement("BlockEntityTag", compoundTag);
        }

        if (this.hasCustomName()) {
            itemstack.setHoverName(this.getCustomName());
        }
        return itemstack;
    }

}

