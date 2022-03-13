package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class ImprovedFallingBlockEntity extends FallingBlockEntity {

    protected boolean saveTileDataToItem;

    public ImprovedFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
        saveTileDataToItem = false;
    }

    public ImprovedFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level, BlockPos pos, BlockState blockState, boolean saveDataToItem) {
        super(type, level);
        this.blocksBuilding = true;
        this.xo = pos.getX() + 0.5D;
        this.yo = pos.getY();
        this.zo = pos.getZ() + 0.5D;
        this.setPos(xo, yo + (double) ((1.0F - this.getBbHeight()) / 2.0F), zo);
        this.setDeltaMovement(Vec3.ZERO);
        this.setStartPos(this.blockPosition());
        this.setBlockState(blockState);
        this.saveTileDataToItem = saveDataToItem;
    }

    public static ImprovedFallingBlockEntity fall(EntityType<? extends FallingBlockEntity> type,
                                          Level level, BlockPos pos, BlockState state, boolean saveDataToItem) {
        ImprovedFallingBlockEntity entity = new ImprovedFallingBlockEntity(type, level, pos, state, saveDataToItem);
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(entity);
        return entity;
    }

    public void setSaveTileDataToItem(boolean b){
        this.saveTileDataToItem = b;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("saveToItem", this.saveTileDataToItem);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.saveTileDataToItem = tag.getBoolean("saveToItem");
    }

    //        this.setHurtsEntities(1f, 20);

    //workaround
    public void setBlockState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("BlockState", NbtUtils.writeBlockState(state));
        tag.putInt("Time", this.time);
        this.readAdditionalSaveData(tag);
    }

    @Override
    public ItemEntity spawnAtLocation(ItemLike itemIn, int offset) {
        ItemStack stack = new ItemStack(itemIn);
        if (itemIn instanceof Block && this.saveTileDataToItem) {
            stack.addTagElement("BlockEntityTag", this.blockData);
        }
        return this.spawnAtLocation(stack, (float) offset);
    }


}
