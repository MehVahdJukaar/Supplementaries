package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.emi.screen.RecipeDisplay;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.items.components.ConfettiColors;
import net.mehvahdjukaar.supplementaries.integration.CompatObjects;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConfettiDyeRecipe extends CustomRecipe {

    private final int maxColors;
    private final Ingredient confetti;

    public ConfettiDyeRecipe(CraftingBookCategory category, Ingredient confetti, int maxColors) {
        super(category);
        this.confetti = confetti;
        this.maxColors = maxColors;
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        int i = 0;
        int j = 0;

        for (int k = 0; k < inv.size(); ++k) {
            ItemStack itemstack = inv.getItem(k);
            if (!itemstack.isEmpty()) {
                if (confetti.test(itemstack)) {
                    ++i;
                    var existing = itemstack.get(ModComponents.CONFETTI_COLORS.get());
                    if (existing != null && existing.size()>=maxColors) return false;

                } else {
                    if (ConfettiColors. getRgbColor(itemstack) == null) {
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

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack itemstack = ItemStack.EMPTY;
        int dyecolor = 0;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (confetti.test(stack)) {
                    itemstack = stack;
                } else {
                    Integer rgb = ConfettiColors.getRgbColor(stack);
                    if (rgb != null) {
                        dyecolor = rgb;
                    }
                }
            }
        }
        ItemStack result = itemstack.copyWithCount(1);
        ConfettiColors colorComp = result.getOrDefault(ModComponents.CONFETTI_COLORS.get(), ConfettiColors.EMPTY);
        result.set(ModComponents.CONFETTI_COLORS.get(),
                colorComp.withAddedColor(dyecolor));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height > 2;
    }

    @Override
    public String getGroup() {
        return "confetti_dye";
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CONFETTI_DYE.get();
    }

    public static class Serializer implements RecipeSerializer<ConfettiDyeRecipe> {

        private static final MapCodec<ConfettiDyeRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                Ingredient.CODEC.fieldOf("ingredient").forGetter((recipe) -> recipe.confetti),
                Codec.INT.fieldOf("max_colors").orElse(4).forGetter((recipe) -> recipe.maxColors)
        ).apply(instance, ConfettiDyeRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ConfettiDyeRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.confetti,
                ByteBufCodecs.INT, recipe -> recipe.maxColors,
                ConfettiDyeRecipe::new);

        @Override
        public MapCodec<ConfettiDyeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ConfettiDyeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

