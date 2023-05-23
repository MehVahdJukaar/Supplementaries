package net.mehvahdjukaar.supplementaries.common.block.tiles;

import net.mehvahdjukaar.supplementaries.common.utils.BlockUtil;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.integration.CaveEnhancementsCompat;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class CandleSkullBlockTile extends EnhancedSkullBlockTile {

    private BlockState candle = Blocks.AIR.defaultBlockState();
    //client only
    private ResourceLocation waxTexture = null;

    public CandleSkullBlockTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.SKULL_CANDLE_TILE.get(), pWorldPosition, pBlockState);
    }

    public ResourceLocation getWaxTexture() {
        return waxTexture;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Candle", NbtUtils.writeBlockState(this.candle));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Candle", 10)) {
            this.candle = MiscUtils.readBlockState(tag.getCompound("Candle"), level);
            if (this.candle.getBlock() instanceof CandleBlock candleBlock) {
                this.waxTexture = getWaxColor(candleBlock);
            } else this.waxTexture = getWaxColor((CandleBlock) Blocks.CANDLE);
        }
    }

    public BlockState getCandle() {
        return candle;
    }

    public void setCandle(BlockState candle) {
        this.candle = candle;
    }

    public boolean tryAddingCandle(CandleBlock candle) {
        if (this.candle.isAir() || (candle == this.candle.getBlock() && this.candle.getValue(CandleBlock.CANDLES) != 4)) {

            if (this.candle.isAir()) {
                this.candle = candle.defaultBlockState();
                this.waxTexture = getWaxColor(candle);
            } else {
                this.candle.cycle(CandleBlock.CANDLES);
            }

            if (!this.level.isClientSide) {
                BlockState state = this.getBlockState();
                BlockState newState = BlockUtil.replaceProperty(this.candle, state, CandleBlock.CANDLES);
                this.level.setBlockAndUpdate(this.worldPosition, newState);
                this.setChanged();
            }
            return true;
        }
        return false;
    }


    @Override
    public void initialize(SkullBlockEntity oldTile, ItemStack stack, Player player, InteractionHand hand) {
        super.initialize(oldTile, stack, player, hand);
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CandleBlock candleBlock) {
            tryAddingCandle(candleBlock);
        }
    }

    @Nullable
    public static ResourceLocation getWaxColor(CandleBlock b) {
        return ModTextures.SKULL_CANDLES_TEXTURES.get().get(b);
    }


    public static void tick(Level level, BlockPos pos, BlockState state, CandleSkullBlockTile e) {
        e.tick(level, pos, state);
        if(CompatHandler.CAVE_ENHANCEMENTS && e.candle.is(CompatObjects.SPECTACLE_CANDLE.get())){
            CaveEnhancementsCompat.tick(level, pos, state);
        }
    }
}
