package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.misc.AntiqueInkHelper;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class AntiqueBookRecipe extends CustomRecipe {
    public AntiqueBookRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;
        Boolean clear = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
            } else if (isBook(stack)) {
                boolean c = AntiqueInkHelper.hasAntiqueInk(stack);
                if (clear != null && c != clear) {
                    return false;
                } else clear = c;

                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            } else if (stack.getItem() == ModRegistry.ANTIQUE_INK.get() || stack.getItem() == ModRegistry.SOAP.get()) {
                boolean c = stack.getItem() == ModRegistry.SOAP.get();
                if (clear != null && c != clear) {
                    return false;
                } else clear = c;
                if (itemstack1 != null) {
                    return false;
                }
                itemstack1 = stack;

            } else return false;
        }
        return itemstack != null && itemstack1 != null;
    }

    private static boolean isBook(ItemStack stack) {
        return (stack.getItem() == Items.WRITTEN_BOOK || stack.getItem() == Items.WRITABLE_BOOK);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        boolean antique = true;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (inv.getItem(i).getItem() == ModRegistry.SOAP.get()) {
                antique = false;
                break;
            }
        }
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (isBook(stack)) {
                ItemStack s = stack.copy();
                s.setCount(1);
                AntiqueInkHelper.setAntiqueInk(s, antique);
                return s;
            }
        }
        return ItemStack.EMPTY;
    }


    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
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
