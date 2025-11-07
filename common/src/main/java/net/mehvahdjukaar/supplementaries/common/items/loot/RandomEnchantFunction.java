package net.mehvahdjukaar.supplementaries.common.items.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class RandomEnchantFunction extends LootItemConditionalFunction {


    public static final MapCodec<RandomEnchantFunction> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            commonFields(instance).and(
                    instance.group(
                            Codec.DOUBLE.fieldOf("chance").forGetter((f) -> f.chance),
                            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).fieldOf("enchantments").forGetter(f -> f.curses)
                    )).apply(instance, RandomEnchantFunction::new));

    private final double chance;
    private final HolderSet<Enchantment> curses;

    public RandomEnchantFunction(List<LootItemCondition> pConditions, double chance, HolderSet<Enchantment> curses) {
        super(pConditions);
        this.chance = chance;
        this.curses = curses;
    }

    @Override
    public LootItemFunctionType<RandomEnchantFunction> getType() {
        return ModRegistry.CURSE_LOOT_FUNCTION.get();
    }

    /**
     * Called to perform the actual action of this function, after conditions have been checked.
     */
    @Override
    public ItemStack run(ItemStack pStack, LootContext context) {

        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(pStack));
        RandomSource random = context.getRandom();
        if (random.nextFloat() < chance && curses.stream().noneMatch(h -> enchantments.getLevel(h) != 0)) {
            var e = curses.getRandomElement(random);
            e.ifPresent(enchantmentHolder -> enchantments.set(enchantmentHolder, 1));
        }

        EnchantmentHelper.setEnchantments(pStack, enchantments.toImmutable());
        return pStack;

    }
}