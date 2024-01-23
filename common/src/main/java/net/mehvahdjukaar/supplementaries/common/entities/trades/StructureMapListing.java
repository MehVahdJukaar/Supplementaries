package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.StrOpt;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

public record StructureMapListing(Item cost, int minPrice, int maxPrice, ItemStack cost2, ResourceLocation structure,
                                  int maxTrades, float priceMult, int level,
                                  String mapName, int mapColor,
                                  ResourceLocation mapMarker) implements ModItemListing {

    public static final Codec<StructureMapListing> CODEC = RecordCodecBuilder.create(i -> i.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(StructureMapListing::cost),
                    StrOpt.of(ExtraCodecs.POSITIVE_INT, "price_min", 7).forGetter(StructureMapListing::minPrice),
                    StrOpt.of(ExtraCodecs.POSITIVE_INT, "price_max", 13).forGetter(StructureMapListing::maxPrice),
                    StrOpt.of(ItemStack.CODEC, "price_secondary", ItemStack.EMPTY).forGetter(StructureMapListing::cost2),
                   ResourceLocation.CODEC.fieldOf("structure").forGetter(p -> p.structure),
                    StrOpt.of(ExtraCodecs.POSITIVE_INT, "max_trades", 16).forGetter(StructureMapListing::maxTrades),
                    StrOpt.of(ExtraCodecs.POSITIVE_FLOAT, "price_multiplier", 0.05f).forGetter(StructureMapListing::priceMult),
                    StrOpt.of(Codec.intRange(1, 5), "level", 1).forGetter(StructureMapListing::level),
                    StrOpt.of(Codec.STRING, "map_name", "").forGetter(p -> p.mapName),
                    StrOpt.of(ColorUtils.CODEC, "map_color", 0xffffff).forGetter(p -> p.mapColor),
                    StrOpt.of(ResourceLocation.CODEC, "map_marker", new ResourceLocation("")).forGetter(p -> p.mapMarker))
            .apply(i, StructureMapListing::new));


    @Override
    public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {

        int i = Math.max(1, random.nextInt(Math.max(1, maxPrice - minPrice)) + minPrice);

        ItemStack itemstack = AdventurerMapsHandler.createCustomMapForTrade(entity.level(), entity.blockPosition(),
                structure, mapName.isEmpty() ? null : mapName, mapColor, mapMarker.getPath().isEmpty() ? null : mapMarker);
        if (itemstack.isEmpty()) return null;

        return new MerchantOffer(new ItemStack(cost, i), cost2, itemstack, maxTrades, ModItemListing.defaultXp(false, level), priceMult);
    }


    @Override
    public Codec<? extends ModItemListing> getCodec() {
        return CODEC;
    }

    @Override
    public int getLevel() {
        return level;
    }
}
