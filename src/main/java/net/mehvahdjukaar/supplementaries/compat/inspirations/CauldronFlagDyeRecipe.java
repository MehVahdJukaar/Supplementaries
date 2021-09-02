package net.mehvahdjukaar.supplementaries.compat.inspirations;

import knightminer.inspirations.library.recipe.RecipeSerializers;
import knightminer.inspirations.library.recipe.cauldron.CauldronContentTypes;
import knightminer.inspirations.library.recipe.cauldron.contents.ICauldronContents;
import knightminer.inspirations.library.recipe.cauldron.inventory.ICauldronInventory;
import knightminer.inspirations.library.recipe.cauldron.special.DyeableCauldronRecipe;
import knightminer.inspirations.library.recipe.cauldron.util.DisplayCauldronRecipe;
import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CauldronFlagDyeRecipe extends DyeableCauldronRecipe {
    public CauldronFlagDyeRecipe(ResourceLocation id, Ingredient ingredient) {
        super(id, ingredient);
    }

    @Override
    public boolean matches(ICauldronInventory inv, World worldIn) {
        ItemStack stack = inv.getStack();
        return inv.getLevel() >= THIRD && this.matches(inv.getContents(), stack);
    }

    private List<DisplayCauldronRecipe> displayRecipes;
    @Override
    public List<DisplayCauldronRecipe> getRecipes() {
        if (this.displayRecipes == null) {
            this.displayRecipes = this.getDisplayRecipes(null).collect(Collectors.toList());
        }

        return this.displayRecipes;
    }

    @Override
    protected boolean matches(ICauldronContents contents, ItemStack stack) {
        Item i = stack.getItem();
        return i instanceof FlagItem && contents.get(CauldronContentTypes.DYE)
                .filter(color -> ((FlagItem) i).getColor() != color).isPresent();
    }

    @Override
    protected ItemStack updateColor(ICauldronContents contents, ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        ItemStack newStack = new ItemStack(ModRegistry.FLAGS.get(contents.get(CauldronContentTypes.DYE).get()).get());
        newStack.setTag(tag);

        return newStack;
    }

    @Override
    protected Stream<DisplayCauldronRecipe> getDisplayRecipes(ItemStack stack) {
        return Arrays.stream(DyeColor.values())
                .map(color -> DisplayCauldronRecipe.builder(THIRD, 0)
                        .setItemInputs(Arrays.stream(DyeColor.values()).filter(c->c!=color)
                                .map(c->new ItemStack(ModRegistry.FLAGS.get(c).get())).collect(Collectors.toList()))
                        .setContentInputs(CauldronContentTypes.DYE.of(color))
                        .setItemOutput(new ItemStack(ModRegistry.FLAGS.get(color).get()))
                        .build());
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RecipeSerializers.CAULDRON_REMOVE_BANNER_PATTERN;
    }


}