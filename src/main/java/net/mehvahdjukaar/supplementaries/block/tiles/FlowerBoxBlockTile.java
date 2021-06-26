package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.ItemShelfBlock;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class FlowerBoxBlockTile extends ItemDisplayTile {

    public BlockState flower0 = Blocks.AIR.defaultBlockState();
    public BlockState flower1 = Blocks.AIR.defaultBlockState();
    public BlockState flower2 = Blocks.AIR.defaultBlockState();

    @Nullable
    public BlockState flower0_up = null;
    @Nullable
    public BlockState flower1_up = null;
    @Nullable
    public BlockState flower2_up = null;

    public FlowerBoxBlockTile() {
        super(Registry.FLOWER_BOX_TILE.get());
        stacks = NonNullList.withSize(3, ItemStack.EMPTY);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.worldPosition).move(0,0.25,0);
    }

    public void updateClientVisualsOnLoad() {

        Item item = this.getItem(0).getItem();
        flower0_up = null;
        if(item instanceof BlockItem){
            flower0 = ((BlockItem)item).getBlock().defaultBlockState();
            if(flower0.getBlock() instanceof DoublePlantBlock){
                flower0_up = flower0.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
            }
        }
        else{
            flower0 = Blocks.AIR.defaultBlockState();
        }

        item = this.getItem(1).getItem();
        flower1_up = null;
        if(item instanceof BlockItem){
            flower1 = ((BlockItem)item).getBlock().defaultBlockState();
            if(flower1.getBlock() instanceof DoublePlantBlock){
                flower1_up = flower1.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
            }
        }
        else{
            flower1 = Blocks.AIR.defaultBlockState();
        }

        item = this.getItem(2).getItem();
        flower2_up = null;
        if(item instanceof BlockItem){
            flower2 = ((BlockItem)item).getBlock().defaultBlockState();
            if(flower2.getBlock() instanceof DoublePlantBlock){
                flower2_up = flower2.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
            }
        }
        else{
            flower2 = Blocks.AIR.defaultBlockState();
        }

    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.flower_box");
    }

    public float getYaw() {
        return -this.getDirection().getOpposite().toYRot();
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(ItemShelfBlock.FACING);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.getItem(index).isEmpty() && stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof BushBlock;
    }

    @Override
    public double getViewDistance() {
        return 64;
    }
}
