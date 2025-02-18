package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.common.misc.map_data.WeatheredHandler;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;

public class WeatheredMapRecipe extends CustomRecipe {

    private final Ingredient ink;
    private final boolean setAntique ;

    public WeatheredMapRecipe(CraftingBookCategory category, Ingredient ink, boolean setAntique) {
        super(category);
        this.ink = ink;
        this.setAntique = setAntique;
    }

    private static WeakReference<ServerLevel> lastLevelHack = null;

    public static void onWorldUnload() {
        lastLevelHack = null;
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
            } else if (isMap(stack)) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            } else if (ink.test(stack)) {
                if (itemstack1 != null) {
                    return false;
                }
                itemstack1 = stack;

            } else return false;
        }
        boolean match = itemstack != null && itemstack1 != null;
        if (match && level instanceof ServerLevel serverLevel) {
            lastLevelHack = new WeakReference<>(serverLevel);
        }
        return match;
    }

    private static boolean isMap(ItemStack stack) {
        return stack.getItem() == Items.FILLED_MAP;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider access) {
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof MapItem) {
                ItemStack s = stack.copy();
                s.setCount(1);
                if (lastLevelHack != null) {
                    WeatheredHandler.setAntique(lastLevelHack.get(), s, setAntique, false);
                    AntiqueInkItem.setAntiqueInk(s, true);
                }
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
        return ModRecipes.ANTIQUE_MAP.get();
    }

    public static class Serializer implements RecipeSerializer<WeatheredMapRecipe> {

        private static final MapCodec<WeatheredMapRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                Ingredient.CODEC.fieldOf("ingredient").forGetter((recipe) -> recipe.ink),
                Codec.BOOL.optionalFieldOf("set_antique", false).forGetter((recipe) -> recipe.setAntique)
        ).apply(instance, WeatheredMapRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, WeatheredMapRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.ink,
                ByteBufCodecs.BOOL,recipe ->recipe.setAntique,
                WeatheredMapRecipe::new);

        @Override
        public MapCodec<WeatheredMapRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WeatheredMapRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
