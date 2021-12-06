package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.PedestalBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class PedestalBlockTile extends ItemDisplayTile {
    //needed on servers for crystals
    public DisplayType type = DisplayType.ITEM;
    public int counter = 0;

    public PedestalBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.PEDESTAL_TILE.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 2, 1));
    }

    public static void clientTick(Level pLevel, BlockPos pPos, BlockState pState, PedestalBlockTile tile) {
        //TODO: do it another way
        tile.counter++;
    }

    @Override
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
        } else if (CommonUtil.isSword(it) || ModTags.PEDESTAL_DOWNRIGHT.contains(it)) {
            this.type = DisplayType.SWORD;
        } else if (it instanceof TridentItem || ModTags.PEDESTAL_UPRIGHT.contains(it)) {
            this.type = DisplayType.TRIDENT;
        } else if (it instanceof EndCrystalItem) {
            this.type = DisplayType.CRYSTAL;
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

