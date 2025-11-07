package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public record PresentItemListing(ModItemListing original) implements ModItemListing {

    public static final MapCodec<PresentItemListing> CODEC = ModItemListing.CODEC.xmap(
            PresentItemListing::new, w -> w.original).fieldOf("trade");

    @Override
    public MerchantOffer getOffer(Entity entity, RandomSource random) {

        MerchantOffer originalOffer = original.getOffer(entity, random);
        if (MiscUtils.FESTIVITY.isChristmas()) {
            Block randomPresent = ModRegistry.PRESENTS.get(DyeColor.values()[
                    random.nextInt(DyeColor.values().length)]).get();
            PresentBlockTile dummyTile = new PresentBlockTile(BlockPos.ZERO,
                    randomPresent.defaultBlockState());
            if (originalOffer == null) return null;
            dummyTile.setItem(0, originalOffer.getResult());
            dummyTile.setSender(entity.getName().getString());
            dummyTile.setPublic();

            ItemStack stack = Utils.saveTileToItem(dummyTile);

            Optional<ItemCost> costB = Optional.ofNullable(itemCost(originalOffer.getCostB()));
            return new MerchantOffer(itemCost(originalOffer.getBaseCostA()), costB, stack, originalOffer.getUses(),
                    originalOffer.getMaxUses(), originalOffer.getXp(), originalOffer.getPriceMultiplier(), originalOffer.getDemand());
        } else return originalOffer;
    }

    private ItemCost itemCost(ItemStack baseCostA) {
        return baseCostA.isEmpty() ? null : new ItemCost(baseCostA.getItem(), baseCostA.getCount());
    }

    @Override
    public MapCodec<? extends ModItemListing> getCodec() {
        return CODEC;
    }

    @Override
    public int getLevel() {
        return original.getLevel();
    }

}
