package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class ItemLoreRecipe extends CustomRecipe {

    private final boolean setLore;
    private final Ingredient requiredIngredient;

    public ItemLoreRecipe(CraftingBookCategory category, Ingredient ingredient, boolean setLore) {
        super(category);
        this.requiredIngredient = ingredient;
        this.setLore = setLore;
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        ItemStack nameTag = null;
        ItemStack item = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (requiredIngredient.test(stack) && (!setLore || stack.has(DataComponents.CUSTOM_NAME))) {
                if (nameTag != null) {
                    return false;
                }
                nameTag = stack;
            } else if (!stack.isEmpty()) {
                if (item != null) {
                    return false;
                }
                item = stack;
            }
        }
        return nameTag != null && item != null &&
                (setLore || !item.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines().isEmpty());
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack nameTag = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (requiredIngredient.test(stack)) {
                nameTag = stack;
            } else if (!stack.isEmpty()) {
                itemstack = stack;
            }
        }
        ItemStack result = itemstack.copyWithCount(1);

        if (!setLore) {
            result.remove(DataComponents.LORE);
        } else {
            Component lore = nameTag.getHoverName();
            result.set(DataComponents.LORE, new ItemLore(List.of(lore)));
        }
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack itemstack = inv.getItem(i);
            if (itemstack.is(Items.NAME_TAG)) {
                ItemStack copy = itemstack.copy();
                copy.setCount(1);
                stacks.set(i, copy);
                return stacks;
            }
        }
        return stacks;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ITEM_LORE.get();
    }

    public static class Serializer implements RecipeSerializer<ItemLoreRecipe> {

        private static final MapCodec<ItemLoreRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                Ingredient.CODEC.fieldOf("ingredient").forGetter((recipe) -> recipe.requiredIngredient),
                Codec.BOOL.fieldOf("set_lore").forGetter((recipe) -> recipe.setLore)
        ).apply(instance, ItemLoreRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ItemLoreRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.requiredIngredient,
                ByteBufCodecs.BOOL, recipe -> recipe.setLore,
                ItemLoreRecipe::new);

        @Override
        public MapCodec<ItemLoreRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ItemLoreRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

