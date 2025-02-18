package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class TatteredBookRecipe extends CustomRecipe {

    private final Ingredient requiredBook = Ingredient.of(Items.WRITTEN_BOOK);
    private final Ingredient requiredInk;
    private final boolean setAntique;

    public TatteredBookRecipe(CraftingBookCategory category, Ingredient antiqueInk, boolean antique) {
        super(category);
        this.requiredInk = antiqueInk;
        this.setAntique = antique;
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {

        ItemStack ink = null;
        ItemStack book = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
            } else if (isValidBook(stack)) {
                if (book != null) {
                    return false;
                }
                book = stack;
            } else if (this.requiredInk.test(stack)) {
                if (ink != null) {
                    return false;
                }
                ink = stack;

            } else return false;
        }
        return book != null && ink != null;
    }

    private boolean isValidBook(ItemStack stack) {
        WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        return content != null && (content.generation() == 0 || !this.setAntique) && this.requiredBook.test(stack) &&
                (AntiqueInkItem.hasAntiqueInk(stack) != this.setAntique);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider access) {
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (isValidBook(stack)) {
                ItemStack s = stack.copy();
                s.setCount(1);
                AntiqueInkItem.setAntiqueInk(s, setAntique);
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ANTIQUE_BOOK.get();
    }


    public static class Serializer implements RecipeSerializer<TatteredBookRecipe> {

        private static final MapCodec<TatteredBookRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                Ingredient.CODEC.fieldOf("ingredient").forGetter((recipe) -> recipe.requiredInk),
                Codec.BOOL.fieldOf("set_antique").forGetter((recipe) -> recipe.setAntique)
        ).apply(instance, TatteredBookRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, TatteredBookRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.requiredInk,
                ByteBufCodecs.BOOL, recipe -> recipe.setAntique,
                TatteredBookRecipe::new);

        @Override
        public MapCodec<TatteredBookRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TatteredBookRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

}
