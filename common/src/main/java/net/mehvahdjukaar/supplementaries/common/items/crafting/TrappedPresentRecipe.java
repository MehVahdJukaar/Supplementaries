package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class TrappedPresentRecipe extends CustomRecipe {

    private final Ingredient tripwire;

    public TrappedPresentRecipe(CraftingBookCategory category, Ingredient tripwire) {
        super(category);
        this.tripwire = tripwire;
    }

    @Override
    public boolean matches(CraftingInput craftingContainer, Level level) {
        int i = 0;
        int j = 0;
        for (int k = 0; k < craftingContainer.size(); ++k) {
            ItemStack itemstack = craftingContainer.getItem(k);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof PresentItem) {
                    var container = itemstack.get(DataComponents.CONTAINER);
                    if (container == null) {
                        return false;
                    }
                    ++i;
                } else {
                    if (!tripwire.test(itemstack)) {
                        return false;
                    }

                    ++j;
                }

                if (j > 1 || i > 1) {
                    return false;
                }
            }
        }

        return i == 1 && j == 1;
    }

    // we could have made this generic with codec but it's not worth it
    @Override
    public ItemStack assemble(CraftingInput craftingContainer, HolderLookup.Provider registryAccess) {
        ItemStack itemstack = ItemStack.EMPTY;
        DyeColor dyecolor = DyeColor.WHITE;
        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack stack = craftingContainer.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (item instanceof PresentItem pi) {
                    itemstack = stack;
                    dyecolor = pi.getColor();
                }
            }
        }
        ItemStack result = itemstack.transmuteCopy(ModRegistry.TRAPPED_PRESENTS.get(dyecolor).get(), 1);
        result.remove(ModComponents.ADDRESS.get());

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TRAPPED_PRESENT.get();
    }

    public static class Serializer implements RecipeSerializer<TrappedPresentRecipe> {

        private static final MapCodec<TrappedPresentRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                Ingredient.CODEC.fieldOf("ingredient").forGetter((recipe) -> recipe.tripwire)
        ).apply(instance, TrappedPresentRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, TrappedPresentRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.tripwire,
                TrappedPresentRecipe::new);

        @Override
        public MapCodec<TrappedPresentRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TrappedPresentRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

