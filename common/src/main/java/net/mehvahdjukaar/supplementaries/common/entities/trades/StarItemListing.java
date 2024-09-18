package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record StarItemListing(ItemCost emeralds, Optional<ItemCost> priceSecondary, int stars,
                              int maxTrades, int xp, float priceMult, int level) implements ModItemListing {
    public static final MapCodec<StarItemListing> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            ItemCost.CODEC.fieldOf("price").forGetter(StarItemListing::emeralds),
            ItemCost.CODEC.optionalFieldOf("price_secondary").forGetter(StarItemListing::priceSecondary),
            Codec.INT.fieldOf("amount").forGetter(StarItemListing::stars),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_trades", 16).forGetter(StarItemListing::maxTrades),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("xp").forGetter(s -> Optional.of(s.xp)),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("price_multiplier", 0.05f).forGetter(StarItemListing::priceMult),
            Codec.intRange(1, 5).optionalFieldOf("level", 1).forGetter(StarItemListing::level)
    ).apply(instance, StarItemListing::createDefault));

    public static StarItemListing createDefault(ItemCost price, Optional<ItemCost> price2, int rockets,
                                                int maxTrades, Optional<Integer> xp, float priceMult,
                                                int level) {
        return new StarItemListing(price, price2, rockets, maxTrades, xp.orElse(ModItemListing.defaultXp(false, level)),
                priceMult, level);
    }

    @Override
    public MerchantOffer getOffer(Entity entity, RandomSource random) {
        ItemStack star = new ItemStack(Items.FIREWORK_STAR, stars);
        star.set(DataComponents.FIREWORK_EXPLOSION, createRandomFireworkStar(random, List.of()));
        return new MerchantOffer(emeralds, priceSecondary, star, maxTrades, xp, priceMult);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public MapCodec<? extends ModItemListing> getCodec() {
        return CODEC;
    }


    private static final DyeColor[] VIBRANT_COLORS = new DyeColor[]{DyeColor.WHITE, DyeColor.ORANGE, DyeColor.MAGENTA, DyeColor.LIGHT_BLUE,
            DyeColor.YELLOW, DyeColor.LIME, DyeColor.PINK, DyeColor.CYAN, DyeColor.PURPLE, DyeColor.BLUE, DyeColor.GREEN, DyeColor.RED};

    public static FireworkExplosion createRandomFireworkStar(RandomSource random, List<FireworkExplosion.Shape> usedShapes) {
        ArrayList<FireworkExplosion.Shape> possible = new ArrayList<>(List.of(FireworkExplosion.Shape.values()));
        possible.removeAll(usedShapes);
        FireworkExplosion.Shape shape;
        boolean twinkle = random.nextFloat() < 0.42f;
        boolean trail = random.nextFloat() < 0.42f;
        if (possible.isEmpty()) {
            shape = FireworkExplosion.Shape.values()[random.nextInt(FireworkExplosion.Shape.values().length)];
        } else {
            shape = possible.get(random.nextInt(possible.size()));
        }
        IntList colors = new IntArrayList();
        int colorCount = 0;
        do {
            colors.add(VIBRANT_COLORS[random.nextInt(VIBRANT_COLORS.length)].getFireworkColor());
            colorCount++;
        } while (random.nextFloat() < 0.42f && colorCount < 9);

        IntList fadeColors = new IntArrayList();
        if (random.nextBoolean()) {
            colorCount = 0;
            do {
                fadeColors.add(VIBRANT_COLORS[random.nextInt(VIBRANT_COLORS.length)].getFireworkColor());
                colorCount++;
            } while (random.nextFloat() < 0.42f && colorCount < 9);
        }
        return new FireworkExplosion(shape, colors, fadeColors, trail, twinkle);
    }

}