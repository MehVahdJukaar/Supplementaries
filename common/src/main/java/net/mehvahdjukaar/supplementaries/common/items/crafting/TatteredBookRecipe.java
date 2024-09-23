package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TatteredBookRecipe extends CustomRecipe {
    public TatteredBookRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;
        Boolean clear = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
            } else if (isValidBook(stack)) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            } else if (stack.getItem() == ModRegistry.ANTIQUE_INK.get()) {
                if (itemstack1 != null) {
                    return false;
                }
                itemstack1 = stack;

            } else return false;
        }
        return itemstack != null && itemstack1 != null;
    }

    private static boolean isValidBook(ItemStack stack) {
        WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        return content == null || content.generation() == 0;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider access) {
        boolean antique = true;
        for (int i = 0; i < inv.size(); ++i) {
            if (inv.getItem(i).getItem() == ModRegistry.SOAP.get()) {
                antique = false;
                break;
            }
        }
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (isValidBook(stack)) {
                ItemStack s = stack.copy();
                s.setCount(1);
                AntiqueInkItem.setAntiqueInk(s, antique);

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


}
