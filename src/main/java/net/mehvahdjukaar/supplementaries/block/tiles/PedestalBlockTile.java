package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.PedestalBlock;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class PedestalBlockTile extends ItemDisplayTile implements ITickableTileEntity {
    public DisplayType type = DisplayType.ITEM;
    public float yaw = 0;
    public int counter = 0;
    public PedestalBlockTile() {
        super(Registry.PEDESTAL_TILE.get());
    }




    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(worldPosition, worldPosition.offset(1, 2, 1));
    }

    @Override
    public void tick() {
        if(this.level.isClientSide)this.counter++;
    }

    @Override
    public void updateOnChanged() {
        //TODO: rewrite this
        if(!this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            boolean hasItem = !this.isEmpty();
            BlockState newstate = state.setValue(PedestalBlock.HAS_ITEM, hasItem)
                    .setValue(PedestalBlock.UP, PedestalBlock.canConnect(level.getBlockState(worldPosition.above()), worldPosition, level, Direction.UP, hasItem));
            if (state != newstate) {
                this.level.setBlock(this.worldPosition, newstate, 3);
            }
        }
        Item it = getDisplayedItem().getItem();
        //TODO: maybe add tag ModTags.isTagged(ModTags.PEDESTAL_DOWNRIGHT,it)
        if (it instanceof BlockItem){
            this.type=DisplayType.BLOCK;
        }
        else if(it instanceof SwordItem || ModTags.isTagged(ModTags.PEDESTAL_DOWNRIGHT,it)){
            this.type=DisplayType.SWORD;
        }
        else if(it instanceof TridentItem || ModTags.isTagged(ModTags.PEDESTAL_UPRIGHT,it)){
            this.type=DisplayType.TRIDENT;
        }
        else if(it instanceof EnderCrystalItem){
            this.type=DisplayType.CRYSTAL;
        }
        else{
            this.type=DisplayType.ITEM;
        }
    }

    //TODO: put yaw inside blockstate so it can be rotated
    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        this.type=DisplayType.values()[compound.getInt("Type")];
        this.yaw=compound.getFloat("Yaw");
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        compound.putInt("Type",this.type.ordinal());
        compound.putFloat("Yaw",this.yaw);
        return compound;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.pedestal");
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    public enum DisplayType{
        ITEM,
        BLOCK,
        SWORD,
        TRIDENT,
        CRYSTAL
    }

}

