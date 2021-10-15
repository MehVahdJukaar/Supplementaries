package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.PedestalBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;

public class PedestalBlockTile extends ItemDisplayTile implements TickableBlockEntity {
    public DisplayType type = DisplayType.ITEM;
    public int counter = 0;

    public PedestalBlockTile() {
        super(ModRegistry.PEDESTAL_TILE.get());
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 2, 1));
    }

    @Override
    public void tick() {
        if (this.level.isClientSide) this.counter++;
    }

    public void updateTileOnInventoryChanged() {

        BlockState state = this.getBlockState();
        boolean hasItem = !this.isEmpty();
        BlockState newState = state.setValue(PedestalBlock.HAS_ITEM, hasItem)
                .setValue(PedestalBlock.UP, PedestalBlock.canConnect(level.getBlockState(worldPosition.above()), worldPosition, level, Direction.UP, hasItem));
        if (state != newState) {
            this.level.setBlock(this.worldPosition, newState, 3);
        }

        //doing this here since I need crystal on server too
        Item it = getDisplayedItem().getItem();
        //TODO: maybe add tag
        if (it instanceof BlockItem) {
            this.type = DisplayType.BLOCK;
        } else if (CommonUtil.isSword(it) || it.is(ModTags.PEDESTAL_DOWNRIGHT)) {
            this.type = DisplayType.SWORD;
        } else if (it instanceof TridentItem || it.is(ModTags.PEDESTAL_UPRIGHT)) {
            this.type = DisplayType.TRIDENT;
        } else if (it instanceof EndCrystalItem) {
            this.type = DisplayType.CRYSTAL;
        } else {
            this.type = DisplayType.ITEM;
        }
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        this.type = DisplayType.values()[compound.getInt("Type")];
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        compound.putInt("Type", this.type.ordinal());
        return compound;
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.pedestal");
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public enum DisplayType {
        ITEM,
        BLOCK,
        SWORD,
        TRIDENT,
        CRYSTAL
    }

}

