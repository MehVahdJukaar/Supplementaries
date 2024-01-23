package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.misc.StrOpt;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record StarItemListing(ItemStack emeralds, ItemStack priceSecondary, int stars,
                              int maxTrades, int xp, float priceMult, int level) implements ModItemListing {
    public static final Codec<StarItemListing> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ItemStack.CODEC.fieldOf("price").forGetter(StarItemListing::emeralds),
            StrOpt.of(ItemStack.CODEC, "price_secondary", ItemStack.EMPTY).forGetter(StarItemListing::priceSecondary),
            Codec.INT.fieldOf("amount").forGetter(StarItemListing::stars),
            StrOpt.of(ExtraCodecs.POSITIVE_INT, "max_trades", 16).forGetter(StarItemListing::maxTrades),
            StrOpt.of(ExtraCodecs.POSITIVE_INT, "xp").forGetter(s -> Optional.of(s.xp)),
            StrOpt.of(ExtraCodecs.POSITIVE_FLOAT, "price_multiplier", 0.05f).forGetter(StarItemListing::priceMult),
            StrOpt.of(Codec.intRange(1, 5), "level", 1).forGetter(StarItemListing::level)
    ).apply(instance, StarItemListing::createDefault));

    public static StarItemListing createDefault(ItemStack price, ItemStack price2, int rockets,
                                                int maxTrades, Optional<Integer> xp, float priceMult,
                                                int level) {
        return new StarItemListing(price, price2, rockets, maxTrades, xp.orElse(ModItemListing.defaultXp(false, level)),
                priceMult, level);
    }

    @Override
    public MerchantOffer getOffer(Entity entity, RandomSource random) {
        ItemStack star = new ItemStack(Items.FIREWORK_STAR, stars);
        star.addTagElement("Explosion", createRandomFireworkStar(random, List.of()));
        return new MerchantOffer(emeralds, priceSecondary, star, maxTrades, xp, priceMult);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public Codec<? extends ModItemListing> getCodec() {
        return CODEC;
    }


    private static final DyeColor[] VIBRANT_COLORS = new DyeColor[]{DyeColor.WHITE, DyeColor.ORANGE, DyeColor.MAGENTA, DyeColor.LIGHT_BLUE,
            DyeColor.YELLOW, DyeColor.LIME, DyeColor.PINK, DyeColor.CYAN, DyeColor.PURPLE, DyeColor.BLUE, DyeColor.GREEN, DyeColor.RED};

    public static CompoundTag createRandomFireworkStar(RandomSource random, List<FireworkRocketItem.Shape> usedShapes) {
        CompoundTag tag = new CompoundTag();
        ArrayList<FireworkRocketItem.Shape> possible = new ArrayList<>(List.of(FireworkRocketItem.Shape.values()));
        possible.removeAll(usedShapes);
        if (possible.isEmpty()) {
            tag.putByte("Type", (byte) FireworkRocketItem.Shape.values()
                    [random.nextInt(FireworkRocketItem.Shape.values().length)].getId());
        } else {
            tag.putByte("Type", (byte) possible.get(random.nextInt(possible.size())).getId());
        }
        tag.putBoolean("Flicker", random.nextFloat() < 0.42f);
        tag.putBoolean("Trail", random.nextFloat() < 0.42f);
        IntList list = new IntArrayList();
        int colors = 0;
        do {
            list.add(VIBRANT_COLORS[random.nextInt(VIBRANT_COLORS.length)].getFireworkColor());
            colors++;
        } while (random.nextFloat() < 0.42f && colors < 9);
        tag.putIntArray("Colors", list);


        if (random.nextBoolean()) {
            IntList fadeList = new IntArrayList();
            colors = 0;
            do {
                fadeList.add(VIBRANT_COLORS[random.nextInt(VIBRANT_COLORS.length)].getFireworkColor());
                colors++;
            } while (random.nextFloat() < 0.42f && colors < 9);
            tag.putIntArray("FadeColors", fadeList);
        }

        return tag;
    }

}