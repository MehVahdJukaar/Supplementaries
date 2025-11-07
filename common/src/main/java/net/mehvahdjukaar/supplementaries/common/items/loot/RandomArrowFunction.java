package net.mehvahdjukaar.supplementaries.common.items.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.List;

public class RandomArrowFunction extends LootItemConditionalFunction {

    public static final MapCodec<RandomArrowFunction> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            commonFields(instance).and(instance.group(
                    Codec.INT.optionalFieldOf("min", 3).forGetter((f) -> f.min),
                    Codec.INT.optionalFieldOf("max", 12).forGetter((f) -> f.max))
            ).apply(instance, RandomArrowFunction::new));

    private static final List<ItemStack> RANDOM_ARROWS = new ArrayList<>();

    //call on mod setup
    public static void setup() {
        for (Holder<Potion> potion : BuiltInRegistries.POTION.holders().toList()) {
            if (potion.is(ModTags.QUIVER_POTION_BLACKLIST)) continue;
            boolean isNegative = false;
            for (var e : potion.value().getEffects()) {
                if (!e.getEffect().value().isBeneficial()) {
                    isNegative = true;
                    break;
                }
            }
            if (isNegative) {
                RANDOM_ARROWS.add(PotionContents.createItemStack(Items.TIPPED_ARROW, potion));
            }
        }
        RANDOM_ARROWS.add(new ItemStack(Items.SPECTRAL_ARROW));
    }

    private final int min;
    private final int max;

    public RandomArrowFunction(List<LootItemCondition> pConditions, int min, int max) {
        super(pConditions);
        this.min = min;
        this.max = max;
    }

    @Override
    public LootItemFunctionType<RandomArrowFunction> getType() {
        return ModRegistry.RANDOM_ARROW_FUNCTION.get();
    }

    /**
     * Called to perform the actual action of this function, after conditions have been checked.
     */
    @Override
    public ItemStack run(ItemStack pStack, LootContext pContext) {

        RandomSource random = pContext.getRandom();
        createRandomQuiver(random, pStack, random.nextInt(min, max + 1));
        return pStack;
    }

    public static ItemStack createRandomQuiver(RandomSource random, float specialMultiplier) {
        ItemStack quiver = new ItemStack(ModRegistry.QUIVER_ITEM.get());

        int amount = random.nextInt(3, (int) (8 + (specialMultiplier * 4)));
        return createRandomQuiver(random, quiver, amount);
    }

    private static ItemStack createRandomQuiver(RandomSource random, ItemStack quiver, int amount) {
        var data = quiver.get(ModComponents.QUIVER_CONTENT.get());
        if (data == null) return quiver;
        var mutable = data.toMutable();
        int tries = 0;
        while (amount > 0 && tries < 10) {
            int stackAmount = random.nextInt(1, 7);
            ItemStack arrow = RANDOM_ARROWS.get(random.nextInt(RANDOM_ARROWS.size())).copy();
            stackAmount = Math.min(amount, stackAmount);
            amount -= stackAmount;
            arrow.setCount(stackAmount);
            mutable.tryAdding(arrow);
            tries++;
        }
        mutable.setSelectedSlot(0);
        quiver.set(ModComponents.QUIVER_CONTENT.get(), mutable.toImmutable());
        return quiver;
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final int min;
        private final int max;

        public Builder() {
            this(3, 12);
        }

        public Builder(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new RandomArrowFunction(this.getConditions(), this.min, this.max);
        }
    }
}