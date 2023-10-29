package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.util.math.colors.BaseColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

import org.jetbrains.annotations.NotNull;
import java.util.function.Function;

public record AdventurerMapTrade(ResourceLocation structure, int villagerLevel, int minPrice, int maxPrice,
                                 String mapName, int mapColor,
                                 ResourceLocation mapMarker) implements VillagerTrades.ItemListing {

    public static final Codec<AdventurerMapTrade> CODEC = RecordCodecBuilder.<AdventurerMapTrade>create(i -> i.group(
                    ResourceLocation.CODEC.fieldOf("structure").forGetter(p -> p.structure),
                    Codec.intRange(0,5).optionalFieldOf("villagerLevel", 2).forGetter(p -> p.villagerLevel),
                    Codec.INT.optionalFieldOf("minPrice", 7).forGetter(p -> p.minPrice),
                    Codec.INT.optionalFieldOf("maxPrice", 13).forGetter(p -> p.maxPrice),
                    Codec.STRING.optionalFieldOf("mapName", "").forGetter(p -> p.mapName),
                    BaseColor.CODEC.optionalFieldOf("mapColor", 0xffffff).forGetter(p -> p.mapColor),
                    ResourceLocation.CODEC.optionalFieldOf("mapMarker", new ResourceLocation("")).forGetter(p -> p.mapMarker))
            .apply(i, AdventurerMapTrade::new)).comapFlatMap(trade -> {
        if (trade.maxPrice < trade.minPrice)
            return DataResult.error("Max price must be larger than min price: [" + trade.minPrice + ", " + trade.maxPrice + "]");
        return DataResult.success(trade);
    }, Function.identity());


    @Override
    public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {

        int i = Math.max(1, random.nextInt(Math.max(1, maxPrice - minPrice)) + minPrice);

        ItemStack itemstack = AdventurerMapsHandler.createCustomMapForTrade(entity.level, entity.blockPosition(),
                structure, mapName.isEmpty() ? null : mapName, mapColor, mapMarker.getPath().isEmpty() ? null : mapMarker);
        if (itemstack.isEmpty()) return null;

        return new MerchantOffer(new ItemStack(Items.EMERALD, i), new ItemStack(Items.COMPASS), itemstack, 12, Math.max(1, 5 * (villagerLevel - 1)), 0.2F);
    }


}