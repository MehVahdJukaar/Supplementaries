package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.items.BubbleBlowerItem;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RepairBubbleBlowerRecipe extends CustomRecipe {

    public RepairBubbleBlowerRecipe(CraftingBookCategory category) {
        super(category);
    }


    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {

        ItemStack bubbleBlower = null;
        ItemStack soap = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ModRegistry.BUBBLE_BLOWER.get()) && hasMissingCharges(stack)) {
                if (bubbleBlower != null) {
                    return false;
                }
                bubbleBlower = stack;
            } else if (stack.is(ModRegistry.SOAP.get())) {
                if (soap != null) {
                    return false;
                }
                soap = stack;
            } else if (!stack.isEmpty()) return false;
        }
        return bubbleBlower != null && soap != null;
    }

    private static boolean hasMissingCharges(ItemStack stack) {
        Integer i = stack.get(ModComponents.CHARGES.get());
        return i == null || i < BubbleBlowerItem.MAX_CHARGES;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider access) {
        ItemStack blower = null;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ModRegistry.BUBBLE_BLOWER.get())) {
                blower = stack;
            }
        }
        ItemStack repaired = blower.copy();
        repaired.set(ModComponents.CHARGES.get(), BubbleBlowerItem.MAX_CHARGES);
        return repaired;

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
        return ModRecipes.BUBBLE_BLOWER_REPAIR.get();
    }


}
