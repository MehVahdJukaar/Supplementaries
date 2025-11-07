package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class SafeRecipe extends CustomRecipe {

    private final Ingredient requiredShulker;
    private final Ingredient requiredIngot;

    public SafeRecipe( CraftingBookCategory category, Ingredient shulker, Ingredient ingot) {
        super( category);
        this.requiredShulker = shulker;
        this.requiredIngot = ingot;
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {

        ItemStack shulker = null;
        ItemStack netherite = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (this.requiredShulker.test(stack)) {
                if (shulker != null) {
                    return false;
                }
                shulker = stack;
            } else if (this.requiredIngot.test(stack)) {
                if (netherite != null) {
                    return false;
                }
                netherite = stack;
            } else if (!stack.isEmpty()) {
                return false;
            }
        }
        return shulker != null && netherite != null;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack shulker = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (this.requiredShulker.test(stack)) {
                shulker = stack;
                break;
            }
        }
        return shulker.transmuteCopy(ModRegistry.SAFE_ITEM.get(), 1);
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SAFE.get();
    }


    public static class Serializer implements RecipeSerializer<SafeRecipe> {

        private static final MapCodec<SafeRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                Ingredient.CODEC.fieldOf("shulker").forGetter((recipe) -> recipe.requiredShulker),
                Ingredient.CODEC.fieldOf("ingot").forGetter((recipe) -> recipe.requiredIngot)
        ).apply(instance, SafeRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, SafeRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.requiredShulker,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.requiredIngot,
                SafeRecipe::new);

        @Override
        public MapCodec<SafeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SafeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

