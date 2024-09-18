package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
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
            if (stack.getItem() == ModRegistry.BUBBLE_BLOWER.get() && stack.getDamageValue() != 0) {
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

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider access) {
        ItemStack blower = null;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == ModRegistry.BUBBLE_BLOWER.get()) {
                blower = stack;
            }
        }
        ItemStack repaired = blower.copy();
        repaired.setDamageValue(0);
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
