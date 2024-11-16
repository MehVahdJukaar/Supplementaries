package net.mehvahdjukaar.supplementaries.common.items.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class SetChargesFunction extends LootItemConditionalFunction {

    public static final MapCodec<SetChargesFunction> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            commonFields(instance).and(
                    IntProvider.CODEC.fieldOf("charges").forGetter((f) -> f.amount)
            ).apply(instance, SetChargesFunction::new));

    private final IntProvider amount;

    public SetChargesFunction(List<LootItemCondition> pConditions, IntProvider min) {
        super(pConditions);
        this.amount = min;
    }

    @Override
    public LootItemFunctionType<SetChargesFunction> getType() {
        return ModRegistry.SET_CHARGES_FUNCTION.get();
    }

    /**
     * Called to perform the actual action of this function, after conditions have been checked.
     */
    @Override
    public ItemStack run(ItemStack pStack, LootContext pContext) {
        RandomSource random = pContext.getRandom();
        pStack.set(ModComponents.CHARGES.get(), amount.sample(random));
        return pStack;
    }

}