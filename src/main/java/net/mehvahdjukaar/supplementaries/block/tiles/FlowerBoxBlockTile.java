package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.ItemShelfBlock;
import net.mehvahdjukaar.supplementaries.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.dynamictrees.DynamicTreesCompat;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
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

import java.util.Objects;

public class FlowerBoxBlockTile extends ItemDisplayTile implements IBlockHolder {

    public static final ModelProperty<BlockState> FLOWER_0 = new ModelProperty<>();

    public static final ModelProperty<BlockState> FLOWER_1 = new ModelProperty<>();

    public static final ModelProperty<BlockState> FLOWER_2 = new ModelProperty<>();


    private final BlockState[] flowerStates = new BlockState[]{Blocks.AIR.defaultBlockState(),
            Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState()};


    public FlowerBoxBlockTile() {
        super(ModRegistry.FLOWER_BOX_TILE.get());
        stacks = NonNullList.withSize(3, ItemStack.EMPTY);
    }

    @Override
    public BlockState getHeldBlock(int index) {
        return flowerStates[index];
    }

    @Override
    public boolean setHeldBlock(BlockState state, int index) {
        if (index >= 0 && index < 3) {
            this.flowerStates[index] = state;
        }
        return false;
    }

    @Override
    public IModelData getModelData() {
        //return data;
        return new ModelDataMap.Builder()
                .withInitial(FLOWER_0, flowerStates[0])
                .withInitial(FLOWER_1, flowerStates[1])
                .withInitial(FLOWER_2, flowerStates[2])
                .build();
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        //this.load(this.getBlockState(), pkt.getTag());
        BlockState oldMimic0 = this.flowerStates[0];
        BlockState oldMimic1 = this.flowerStates[1];
        BlockState oldMimic2 = this.flowerStates[2];
        CompoundNBT tag = pkt.getTag();
        handleUpdateTag(this.getBlockState(), tag);
        if (!Objects.equals(oldMimic0, this.flowerStates[0]) || !Objects.equals(oldMimic1, this.flowerStates[1]) ||
                !Objects.equals(oldMimic2, this.flowerStates[2])) {
            ModelDataManager.requestModelDataRefresh(this);
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.worldPosition).move(0, 0.25, 0);
    }

    @Override
    public void updateClientVisualsOnLoad() {

        for (int n = 0; n < flowerStates.length; n++) {
            Item item = this.getItem(n).getItem();
            Block b = null;
            if (item instanceof BlockItem) {
                b = ((BlockItem) item).getBlock();
            } else if (CompatHandler.dynamictrees) {
                b = DynamicTreesCompat.getOptionalDynamicSapling(item, this.level, this.worldPosition);
            }
            if (b == null) b = Blocks.AIR;
            this.flowerStates[n] = b.defaultBlockState();

        }

        //TODO: check this
        ModelDataManager.requestModelDataRefresh(this);
        this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);

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
        if (this.getItem(index).isEmpty()) {
            Item item = stack.getItem();
            return item.is(ModTags.FLOWER_BOX_PLANTABLE) || FlowerPotHandler.hasSpecialFlowerModel(item);
        }
        return false;
    }
}
