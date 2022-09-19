package net.mehvahdjukaar.supplementaries.common.items.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomArrowFunction extends LootItemConditionalFunction {

    private static final List<ItemStack> RANDOM_ARROWS = new ArrayList<>();

    //call on mod setup
    public static void setup() {
        for (Potion potion : Registry.POTION) {
            boolean isNegative = false;
            for (var e : potion.getEffects()) {
                if (!e.getEffect().isBeneficial()) {
                    isNegative = true;
                    break;
                }
            }
            if (isNegative) {
                RANDOM_ARROWS.add(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), potion));
            }
        }
        RANDOM_ARROWS.add(new ItemStack(Items.SPECTRAL_ARROW));
    }

    final int min;
    final int max;

    RandomArrowFunction(LootItemCondition[] pConditions, int min, int max) {
        super(pConditions);
        this.min = min;
        this.max = max;
    }

    @Override
    public LootItemFunctionType getType() {
        return ModRegistry.RANDOM_ARROW_FUNCTION;
    }

    /**
     * Called to perform the actual action of this function, after conditions have been checked.
     */
    @Override
    public ItemStack run(ItemStack pStack, LootContext pContext) {

        Random random = pContext.getRandom();
        createRandomQuiver(random, pStack, random.nextInt(min, max + 1));
        return pStack;
    }

    public static ItemStack createRandomQuiver(Random random, float specialMultiplier) {
        ItemStack quiver = new ItemStack(ModRegistry.QUIVER_ITEM.get());

        int amount = random.nextInt(3, (int)Math.max(6,(8 + (specialMultiplier * 4))));
        return createRandomQuiver(random, quiver, amount);
    }

    private static ItemStack createRandomQuiver(Random random, ItemStack quiver, int amount) {
        var data = QuiverItem.getQuiverData(quiver);
        if (data == null) return quiver;
        int tries = 0;
        while (amount > 0 && tries < 10) {
            int stackAmount = random.nextInt(1, 7);
            ItemStack arrow = RANDOM_ARROWS.get(random.nextInt(RANDOM_ARROWS.size())).copy();
            stackAmount = Math.min(amount, stackAmount);
            amount -= stackAmount;
            arrow.setCount(stackAmount);
            data.tryAdding(arrow);
            tries++;
        }
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

    public static class Serializer extends LootItemConditionalFunction.Serializer<RandomArrowFunction> {
        /**
         * Serialize the value by putting its data into the JsonObject.
         */
        @Override
        public void serialize(JsonObject jsonObject, RandomArrowFunction function, JsonSerializationContext context) {
            super.serialize(jsonObject, function, context);
            jsonObject.addProperty("min", function.min);
            jsonObject.addProperty("max", function.max);
        }

        @Override
        public RandomArrowFunction deserialize(JsonObject pObject, JsonDeserializationContext context, LootItemCondition[] pConditions) {

            int min = GsonHelper.getAsInt(pObject, "min", 3);
            int max = GsonHelper.getAsInt(pObject, "max", 12);
            return new RandomArrowFunction(pConditions, min, max);
        }
    }
}