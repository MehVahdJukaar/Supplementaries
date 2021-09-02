package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.ItemShelfBlock;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Objects;

public class FlowerBoxBlockTile extends ItemDisplayTile {

    public static final ModelProperty<BlockState> FLOWER_0 = new ModelProperty<>();;
    public static final ModelProperty<BlockState> FLOWER_1 = new ModelProperty<>();;
    public static final ModelProperty<BlockState> FLOWER_2 = new ModelProperty<>();;

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
        super(ModRegistry.FLOWER_BOX_TILE.get());
        stacks = NonNullList.withSize(3, ItemStack.EMPTY);
    }

    @Override
    public IModelData getModelData() {
        //return data;
        return new ModelDataMap.Builder()
                .withInitial(FLOWER_0, flower0)
                .withInitial(FLOWER_1, flower1)
                .withInitial(FLOWER_2, flower2)
                .build();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        //this.load(this.getBlockState(), pkt.getTag());
        BlockState oldMimic0 = this.flower0;
        BlockState oldMimic1 = this.flower1;
        BlockState oldMimic2 = this.flower2;
        CompoundNBT tag = pkt.getTag();
        handleUpdateTag(this.getBlockState(), tag);
        if (!Objects.equals(oldMimic0, this.flower0) || !Objects.equals(oldMimic1, this.flower1) || !Objects.equals(oldMimic2, this.flower2)) {
            ModelDataManager.requestModelDataRefresh(this);
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.worldPosition).move(0,0.25,0);
    }

    @Override
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


        ModelDataManager.requestModelDataRefresh(this);


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
        return this.getItem(index).isEmpty() && stack.getItem().is(ModTags.FLOWER_BOX_PLANTABLE) ;
    }

    @Override
    public double getViewDistance() {
        return 64;
    }
}
