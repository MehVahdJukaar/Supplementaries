package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.common.items.RopeArrowItem;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class RopeArrowRecipe extends CustomRecipe {

    private final Ingredient targetArrow;
    private final Ingredient targetRope;

    public RopeArrowRecipe(CraftingBookCategory category, Ingredient arrow, Ingredient rope) {
        super(category);
        this.targetArrow = arrow;
        this.targetRope = rope;
    }


    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {

        ItemStack arrow = null;
        ItemStack rope = null;
        int missingRopes = 0;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (targetArrow.test(stack)) {
                if (arrow != null) {
                    return false;
                }
                arrow = stack;
                missingRopes += RopeArrowItem.getRopes(arrow);
            } else if (targetRope.test(stack)) {
                rope = stack;
                missingRopes--;
            } else if (!stack.isEmpty()) return false;
        }
        return arrow != null && rope != null && missingRopes >= 0;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider access) {
        int ropes = 0;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (targetRope.test(stack)) {
                ropes++;
            }
        }
        ItemStack returnArrow = ModRegistry.ROPE_ARROW_ITEM.get().getDefaultInstance();
        RopeArrowItem.addRopes(returnArrow, ropes);
        return returnArrow;

    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ROPE_ARROW_ADD.get();
    }

    public static class Serializer implements RecipeSerializer<RopeArrowRecipe> {

        private static final MapCodec<RopeArrowRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                Ingredient.CODEC.fieldOf("arrow").forGetter((recipe) -> recipe.targetArrow),
                Ingredient.CODEC.fieldOf("rope").forGetter((recipe) -> recipe.targetRope)
        ).apply(instance, RopeArrowRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, RopeArrowRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.targetArrow,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.targetRope,
                RopeArrowRecipe::new);

        @Override
        public MapCodec<RopeArrowRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RopeArrowRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
