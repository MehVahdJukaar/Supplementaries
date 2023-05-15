package net.mehvahdjukaar.supplementaries.common.block.tiles;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.blocks.PedestalBlock;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class PedestalBlockTile extends ItemDisplayTile {
    //needed on servers for crystals
    private DisplayType type = DisplayType.ITEM;

    public PedestalBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.PEDESTAL_TILE.get(), pos, state);
    }

    @PlatformOnly(PlatformOnly.FORGE)
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 2, 1));
    }

    public DisplayType getDisplayType() {
        return type;
    }

    @Override
    public void updateTileOnInventoryChanged() {

        BlockState state = this.getBlockState();
        boolean hasItem = !this.isEmpty();
        BlockState newState = state
                .setValue(PedestalBlock.ITEM_STATUS, PedestalBlock.getStatus(this.level, this.worldPosition, hasItem))
                .setValue(PedestalBlock.UP, PedestalBlock.canConnectTo(level.getBlockState(worldPosition.above()), worldPosition, level, Direction.UP, hasItem));
        if (state != newState) {
            this.level.setBlock(this.worldPosition, newState, 3);
            if(!state.getValue(PedestalBlock.ITEM_STATUS).hasTile()){
                this.level.removeBlockEntity(this.worldPosition);
            }
        }

        //doing this here since I need crystal on server too
        ItemStack stack = getDisplayedItem();
        Item it = stack.getItem();

        if (MiscUtils.isSword(it) || stack.is(ModTags.PEDESTAL_DOWNRIGHT)) {
            this.type = DisplayType.SWORD;
        } else if (it instanceof TridentItem || stack.is(ModTags.PEDESTAL_UPRIGHT)) {
            this.type = DisplayType.TRIDENT;
        } else if (it instanceof EndCrystalItem) {
            this.type = DisplayType.CRYSTAL;
        } else if (it == ModRegistry.GLOBE_ITEM.get()) {
            this.type = DisplayType.GLOBE;
        } else if (it == ModRegistry.GLOBE_SEPIA_ITEM.get()) {
            this.type = DisplayType.SEPIA_GLOBE;
        } else if (it instanceof BlockItem) {
            this.type = DisplayType.BLOCK;
        } else {
            this.type = DisplayType.ITEM;
        }
    }


    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        //needed on client
        this.type = DisplayType.values()[compound.getInt("Type")];
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Type", this.type.ordinal());
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.pedestal");
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
        CRYSTAL,
        GLOBE,
        SEPIA_GLOBE;

        public boolean isGlobe() {
            return this == GLOBE || this == SEPIA_GLOBE;
        }
    }

}

