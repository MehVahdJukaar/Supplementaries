package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SusGravelBricksTile extends RandomizableContainerBlockEntity {

    protected NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public SusGravelBricksTile(BlockPos pos, BlockState blockState) {
        super(ModRegistry.SUS_GRAVEL_BRICKS_TILE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if(!this.items.get(0).isEmpty()){
            tag.put("item", this.items.get(0).save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("item", 10)) {
            this.items.set(0, ItemStack.of(tag.getCompound("item")));
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemStacks) {
        this.items = itemStacks;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean canTakeItem(Container container, int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canOpen(Player player) {
        return false;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.supplementaries.suspicious_gravel_bricks");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

}

