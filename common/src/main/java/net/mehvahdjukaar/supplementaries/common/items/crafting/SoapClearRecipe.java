package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.supplementaries.common.utils.SoapWashableHelper;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class SoapClearRecipe extends CustomRecipe {

    private final Ingredient soap;

    public SoapClearRecipe(CraftingBookCategory category, Ingredient soap) {
        super(category);
        this.soap = soap;
    }

    @Override
    public boolean matches(CraftingInput craftingContainer, Level level) {
        int i = 0;
        int j = 0;

        for (int k = 0; k < craftingContainer.size(); ++k) {
            ItemStack itemstack = craftingContainer.getItem(k);
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                boolean isColored = (BlocksColorAPI.getColor(item) != null &&
                        SoapWashableHelper.canCleanColor(item));
                if (isColored || itemstack.has(DataComponents.DYED_COLOR) || hasTrim(item)) {
                    ++i;
                } else {
                    if (!soap.test(itemstack)) {
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

    //TODO: add this and JEI view of it
    private boolean hasTrim(Item item) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack toRecolor = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (BlocksColorAPI.getColor(item) != null ||
                        stack.has(DataComponents.DYED_COLOR) || hasTrim(item)) {
                    toRecolor = stack.copyWithCount(1);
                }
            }
        }

        ItemStack result;
        Item i = toRecolor.getItem();
        if (toRecolor.has(ModComponents.CONFETTI_COLORS.get())) {
            result = toRecolor.copy();
            result.remove(ModComponents.CONFETTI_COLORS.get());
            return result;
        } else if (toRecolor.has(DataComponents.DYED_COLOR)) {
            result = toRecolor.copy();
            ItemStack def = toRecolor.getItem().getDefaultInstance();
            if (def.has(DataComponents.DYED_COLOR)) {
                result.set(DataComponents.DYED_COLOR, def.get(DataComponents.DYED_COLOR));
            } else {
                result.remove(DataComponents.DYED_COLOR);
            }
            return result;
        } else {
            Item recolored = BlocksColorAPI.changeColor(i, null);
            if (recolored != null) {
                result = toRecolor.transmuteCopy(recolored, 1);
            } else {
                result = toRecolor.copy();
            }
        }

        result.setCount(1);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SOAP_CLEARING.get();
    }


    public static class Serializer implements RecipeSerializer<SoapClearRecipe> {

        private static final MapCodec<SoapClearRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                Ingredient.CODEC.fieldOf("ingredient").forGetter((recipe) -> recipe.soap)
        ).apply(instance, SoapClearRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, SoapClearRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.soap,
                SoapClearRecipe::new);

        @Override
        public MapCodec<SoapClearRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SoapClearRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

