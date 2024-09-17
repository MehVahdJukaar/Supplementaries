package net.mehvahdjukaar.supplementaries.common.entities.trades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.trades.ModItemListing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.mehvahdjukaar.supplementaries.common.entities.trades.StarItemListing.createRandomFireworkStar;

public record RocketItemListing(ItemStack emeralds, ItemStack priceSecondary, int rockets,
                                int maxTrades, int xp, float priceMult, int level) implements ModItemListing {

    public static final MapCodec<RocketItemListing> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            ItemStack.CODEC.fieldOf("price").forGetter(RocketItemListing::emeralds),
            ItemStack.CODEC.optionalFieldOf("price_secondary", ItemStack.EMPTY).forGetter(RocketItemListing::priceSecondary),
            Codec.INT.fieldOf("amount").forGetter(RocketItemListing::rockets),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_trades", 16).forGetter(RocketItemListing::maxTrades),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("xp").forGetter(s -> Optional.of(s.xp)),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("price_multiplier", 0.05f).forGetter(RocketItemListing::priceMult),
            Codec.intRange(1, 5).optionalFieldOf("level", 1).forGetter(RocketItemListing::level)
    ).apply(instance, RocketItemListing::createDefault));

    public static RocketItemListing createDefault(ItemStack price, ItemStack price2, int rockets,
                                                  int maxTrades, Optional<Integer> xp, float priceMult,
                                                  int level) {
        return new RocketItemListing(price, price2, rockets, maxTrades, xp.orElse(ModItemListing.defaultXp(false, level)),
                priceMult, level);
    }

    @Override
    public MerchantOffer getOffer(Entity entity, RandomSource random) {

        ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, rockets);
        CompoundTag tag = itemstack.getOrCreateTagElement("Fireworks");
        ListTag listTag = new ListTag();

        int stars = 0;
        List<FireworkExplosion.Shape> usedShapes = new ArrayList<>();
        do {
            listTag.add(createRandomFireworkStar(random, usedShapes));
            stars++;
        } while (random.nextFloat() < 0.42f && stars < 7);

        tag.putByte("Flight", (byte) (random.nextInt(3) + 1));
        tag.put("Explosions", listTag);

        return new MerchantOffer(emeralds, priceSecondary, itemstack, maxTrades, ModItemListing.defaultXp(true, level),
                priceMult);
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
