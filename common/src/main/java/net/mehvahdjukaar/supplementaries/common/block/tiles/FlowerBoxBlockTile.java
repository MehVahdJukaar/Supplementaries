package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.impl.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.blocks.ItemShelfBlock;
import net.mehvahdjukaar.supplementaries.common.block.util.IBlockHolder;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.dynamictrees.DynamicTreesCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Objects;

public class FlowerBoxBlockTile extends ItemDisplayTile implements IBlockHolder {

    public static final ModelProperty<BlockState> FLOWER_0 = new ModelProperty<>();

    public static final ModelProperty<BlockState> FLOWER_1 = new ModelProperty<>();

    public static final ModelProperty<BlockState> FLOWER_2 = new ModelProperty<>();


    private final BlockState[] flowerStates = new BlockState[]{Blocks.AIR.defaultBlockState(),
            Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState()};

    public FlowerBoxBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.FLOWER_BOX_TILE.get(), pos, state, 3);
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        //this.load(this.getBlockState(), pkt.getTag());
        BlockState oldMimic0 = this.flowerStates[0];
        BlockState oldMimic1 = this.flowerStates[1];
        BlockState oldMimic2 = this.flowerStates[2];
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);
        if (!Objects.equals(oldMimic0, this.flowerStates[0]) || !Objects.equals(oldMimic1, this.flowerStates[1]) ||
                !Objects.equals(oldMimic2, this.flowerStates[2])) {
            ModelDataManager.requestModelDataRefresh(this);
            if (level != null) {
                this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).move(0, 0.25, 0);
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
        if (level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.flower_box");
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
            return stack.is(ModTags.FLOWER_BOX_PLANTABLE) || FlowerPotHandler.hasSpecialFlowerModel(stack.getItem());
        }
        return false;
    }
}
