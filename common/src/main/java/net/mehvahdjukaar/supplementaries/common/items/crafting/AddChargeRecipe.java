package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.common.items.RopeArrowItem;
import net.mehvahdjukaar.supplementaries.common.misc.mob_container.BucketHelper;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class AddChargeRecipe extends CustomRecipe {

    private final Ingredient targetItem;
    private final Ingredient targetCharge;
    private final Item result;
    private final int chargesPerItem;
    private final boolean canOverflow;

    public AddChargeRecipe(CraftingBookCategory category, Ingredient arrow, Ingredient rope, Item result,
                           int chargesPerItem, boolean canOverflow) {
        super(category);
        this.targetItem = arrow;
        this.targetCharge = rope;
        this.result = result;
        this.chargesPerItem = chargesPerItem;
        this.canOverflow = canOverflow;
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {

        ItemStack arrow = null;
        ItemStack rope = null;
        int newTotalCharges = 0;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (targetItem.test(stack)) {
                if (arrow != null) {
                    return false;
                }
                arrow = stack;
                newTotalCharges += stack.getOrDefault(ModComponents.CHARGES.get(), 0);
            } else if (targetCharge.test(stack)) {
                rope = stack;
                newTotalCharges += chargesPerItem;
            } else if (!stack.isEmpty()) return false;
        }
        return arrow != null && rope != null && (canOverflow || newTotalCharges <=
                arrow.getOrDefault(ModComponents.MAX_CHARGES.get(), 0));
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider access) {
        int newTotalCharges = 0;
        ItemStack arrow = null;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (targetItem.test(stack)) {
                arrow = stack;
                newTotalCharges += stack.getOrDefault(ModComponents.CHARGES.get(), 0);
            } else if (targetCharge.test(stack)) {
                newTotalCharges += chargesPerItem;
            }
        }
        ItemStack returnArrow = arrow.transmuteCopy(result, 1);
        returnArrow.set(ModComponents.CHARGES.get(), Math.min(newTotalCharges,
                returnArrow.getOrDefault(ModComponents.MAX_CHARGES.get(), 0)));
        return returnArrow;

    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ROPE_ARROW_ADD.get();
    }

    public static class Serializer implements RecipeSerializer<AddChargeRecipe> {

        private static final MapCodec<AddChargeRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                Ingredient.CODEC.fieldOf("ingredient").forGetter((recipe) -> recipe.targetItem),
                Ingredient.CODEC.fieldOf("charge").forGetter((recipe) -> recipe.targetCharge),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter((recipe) -> recipe.result),
                Codec.INT.optionalFieldOf("charges_per_item", 1).forGetter((recipe) -> recipe.chargesPerItem),
                Codec.BOOL.optionalFieldOf("can_overfill", false).forGetter((recipe) -> recipe.canOverflow)
        ).apply(instance, AddChargeRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, AddChargeRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.targetItem,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.targetCharge,
                ByteBufCodecs.registry(Registries.ITEM), recipe -> recipe.result,
                ByteBufCodecs.VAR_INT, recipe -> recipe.chargesPerItem,
                ByteBufCodecs.BOOL, recipe -> recipe.canOverflow,
                AddChargeRecipe::new);

        @Override
        public MapCodec<AddChargeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AddChargeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
