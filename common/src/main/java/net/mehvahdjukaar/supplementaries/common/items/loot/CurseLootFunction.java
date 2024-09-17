package net.mehvahdjukaar.supplementaries.common.items.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CurseLootFunction extends LootItemConditionalFunction {

    private static final List<Enchantment> CURSES = new ArrayList<>();

    //call on mod setup
    public static void rebuild() {
        for (var e : BuiltInRegistries.ENCHANTMENT) {
            if (e.isCurse() && !BuiltInRegistries.ENCHANTMENT.wrapAsHolder(e).is(ModTags.CURSES_BLACKLIST)) CURSES.add(e);
        }
    }

    final double chance;

    CurseLootFunction(LootItemCondition[] pConditions, double chance) {
        super(pConditions);
        this.chance = chance;
    }

    @Override
    public LootItemFunctionType<?> getType() {
        return ModRegistry.CURSE_LOOT_FUNCTION.get();
    }

    /**
     * Called to perform the actual action of this function, after conditions have been checked.
     */
    @Override
    public ItemStack run(ItemStack pStack, LootContext pContext) {

        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(pStack);

        RandomSource random = pContext.getRandom();

        if (random.nextFloat() < chance && CURSES.stream().noneMatch(map::containsKey) && !CURSES.isEmpty()) {

            Enchantment e = CURSES.get(random.nextInt(CURSES.size()));
            map.put(e, 1);
        }

        EnchantmentHelper.setEnchantments(map, pStack);
        return pStack;

    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private final double chance;

        public Builder() {
            this(1);
        }

        public Builder(double chance) {
            this.chance = chance;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CurseLootFunction(this.getConditions(), this.chance);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CurseLootFunction> {
        /**
         * Serialize the value by putting its data into the JsonObject.
         */
        @Override
        public void serialize(JsonObject jsonObject, CurseLootFunction function, JsonSerializationContext context) {
            super.serialize(jsonObject, function, context);
            jsonObject.addProperty("chance", function.chance);
        }

        @Override
        public CurseLootFunction deserialize(JsonObject pObject, JsonDeserializationContext context, LootItemCondition[] pConditions) {

            double chance = GsonHelper.getAsDouble(pObject, "chance", 1);
            return new CurseLootFunction(pConditions, chance);
        }
    }
}