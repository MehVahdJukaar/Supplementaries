package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

public record RandomAdventurerMapListing(Item emerald, int priceMin, int priceMax, ItemStack priceSecondary,
                                         int maxTrades, float priceMult, int level) implements ModItemListing {

    public static final MapCodec<RandomAdventurerMapListing> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item", Items.EMERALD).forGetter(RandomAdventurerMapListing::emerald),
            Codec.INT.fieldOf("price_min").forGetter(RandomAdventurerMapListing::priceMin),
            Codec.INT.fieldOf("price_max").forGetter(RandomAdventurerMapListing::priceMax),
            ItemStack.CODEC.optionalFieldOf("price_secondary", ItemStack.EMPTY).forGetter(RandomAdventurerMapListing::priceSecondary),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_trades", 16).forGetter(RandomAdventurerMapListing::maxTrades),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("price_multiplier", 0.05f).forGetter(RandomAdventurerMapListing::priceMult),
            Codec.intRange(1, 5).optionalFieldOf("level", 1).forGetter(RandomAdventurerMapListing::level)
    ).apply(i, RandomAdventurerMapListing::new));


    @Override
    public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {
        int emeraldCost = random.nextInt(priceMax - priceMin + 1) + priceMax;

        if (entity.level() instanceof ServerLevel serverLevel) {
            ItemStack result = AdventurerMapsHandler.createMapOrQuill(serverLevel, entity.blockPosition(), null,
                    AdventurerMapsHandler.SEARCH_RADIUS, true, 2, null, "filled_map.adventure", 0x78151a);

            int x = 6;
            int xp = (int) ((x * 12) / (float) maxTrades);

            return new MerchantOffer(new ItemStack(emerald, emeraldCost), priceSecondary, result, maxTrades, xp, priceMult);
        }
        return null;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public MapCodec<? extends ModItemListing> getCodec() {
        return CODEC;
    }
}

