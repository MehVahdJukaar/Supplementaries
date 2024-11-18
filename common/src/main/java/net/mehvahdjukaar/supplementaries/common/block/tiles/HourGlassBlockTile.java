package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.blocks.HourGlassBlock;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimeData;
import net.mehvahdjukaar.supplementaries.common.block.hourglass.HourglassTimesManager;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class HourGlassBlockTile extends ItemDisplayTile {
    private HourglassTimeData sandData = HourglassTimeData.EMPTY;
    private float progress = 0; //0-1 percentage of progress
    private float prevProgress = 0;
    private int power = 0;
    //client
    private ResourceLocation cachedTexture = null;

    public HourGlassBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.HOURGLASS_TILE.get(), pos, state);
    }

    @Override
    public void updateTileOnInventoryChanged() {
        this.sandData = HourglassTimesManager.getInstance(level).getData(this.getDisplayedItem().getItem());
        int p = this.getDirection() == Direction.DOWN ? 1 : 0;
        int l = this.sandData.light();
        if (l != this.getBlockState().getValue(HourGlassBlock.LIGHT_LEVEL)) {
            if (this.level != null)
                level.setBlock(this.worldPosition, this.getBlockState()
                        .setValue(HourGlassBlock.LIGHT_LEVEL, l), 4 | 16);
        }
        this.prevProgress = p;
        this.progress = p;
    }

    public HourglassTimeData getSandData() {
        return sandData;
    }

    public float getProgress(float partialTicks) {
       return Mth.lerp(partialTicks, this.prevProgress, this.progress);
    }

    public ResourceLocation getTexture() {
        if (this.cachedTexture == null) {
            this.cachedTexture = this.sandData.computeTexture(this.getDisplayedItem(), this.level);
        }
        return this.cachedTexture;
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, HourGlassBlockTile tile) {
        Direction dir = pState.getValue(HourGlassBlock.FACING);
        if (!tile.sandData.isEmpty()) {
            tile.prevProgress = tile.progress;
            if (dir == Direction.UP && tile.progress != 1) {
                tile.progress = Math.min(tile.progress + tile.sandData.getIncrement(), 1f);
            } else if (dir == Direction.DOWN && tile.progress != 0) {
                tile.progress = Math.max(tile.progress - tile.sandData.getIncrement(), 0f);
            }
        }

        if (!pLevel.isClientSide) {
            int p;
            if (dir == Direction.DOWN) {
                p = 1 + (int) ((1 - tile.progress) * 14f);
            } else {
                p = 1 + (int) ((tile.progress) * 14f);
            }
            if (p != tile.power) {
                tile.power = p;
                pLevel.updateNeighbourForOutputSignal(pPos, pState.getBlock());
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getFloat("Progress");
        this.prevProgress = tag.getFloat("PrevProgress");
        this.cachedTexture = null;
        this.sandData = HourglassTimesManager.getInstance(level).getData(this.getDisplayedItem().getItem());
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("Progress", this.progress);
        tag.putFloat("PrevProgress", this.prevProgress);
    }

    @Override
    public Component getDefaultName() {
        return Component.translatable("block.supplementaries.hourglass");
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.isEmpty() && !HourglassTimesManager.getInstance(level).getData(stack.getItem()).isEmpty();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        if (direction == Direction.UP) {
            return this.canPlaceItem(0, stack);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        Direction dir = this.getBlockState().getValue(HourGlassBlock.FACING);
        return (dir == Direction.UP && this.progress == 1) || (dir == Direction.DOWN && this.progress == 0);
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(HourGlassBlock.FACING);
    }

    public int getPower() {
        return power;
    }

    //TODO: better sound event
    @Override
    public SoundEvent getAddItemSound() {
        return SoundEvents.SAND_PLACE;
    }
}