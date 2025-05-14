package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public record StructureMapListing(Item cost, int minPrice, int maxPrice, Optional<ItemCost> cost2,
                                  HolderSet<Structure> structure,
                                  int maxTrades, float priceMult, int level,
                                  String mapName, int mapColor,
                                  ResourceLocation mapMarker) implements ModItemListing {

    // tag single or list
    private static final Codec<HolderSet<Structure>> TARGET_CODEC = Codec.either(
                    RegistryCodecs.homogeneousList(Registries.STRUCTURE, true), Structure.CODEC)
            .xmap(either -> either.map(Function.identity(), HolderSet::direct), Either::left);


    public static final MapCodec<StructureMapListing> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(StructureMapListing::cost),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("price_min", 7).forGetter(StructureMapListing::minPrice),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("price_max", 13).forGetter(StructureMapListing::maxPrice),
                    ItemCost.CODEC.optionalFieldOf("price_secondary").forGetter(StructureMapListing::cost2),
                    TARGET_CODEC.fieldOf("structure").forGetter(p -> p.structure),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_trades", 16).forGetter(StructureMapListing::maxTrades),
                    ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("price_multiplier", 0.05f).forGetter(StructureMapListing::priceMult),
                    Codec.intRange(1, 5).optionalFieldOf("level", 1).forGetter(StructureMapListing::level),
                    Codec.STRING.optionalFieldOf("map_name", "").forGetter(p -> p.mapName),
                    ColorUtils.CODEC.optionalFieldOf("map_color", 0xffffff).forGetter(p -> p.mapColor),
                    ResourceLocation.CODEC.optionalFieldOf("map_marker", ResourceLocation.parse("")).forGetter(p -> p.mapMarker))
            .apply(i, StructureMapListing::new));


    @Override
    public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {
        ItemStack itemstack = AdventurerMapsHandler.createCustomMapForTrade(entity.level(), entity.blockPosition(),
                structure, mapName.isEmpty() ? null : mapName, mapColor, mapMarker.getPath().isEmpty() ? null : mapMarker);
        if (itemstack.isEmpty()) return null;

        int i = Math.max(1, random.nextInt(Math.max(1, maxPrice - minPrice)) + minPrice);
        return new MerchantOffer(new ItemCost(cost, i), cost2, itemstack, maxTrades, ModItemListing.defaultXp(false, level), priceMult);
    }


    @Override
    public MapCodec<? extends ModItemListing> getCodec() {
        return CODEC;
    }

    @Override
    public int getLevel() {
        return level;
    }
}
