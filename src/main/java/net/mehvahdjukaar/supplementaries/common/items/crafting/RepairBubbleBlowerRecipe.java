package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RepairBubbleBlowerRecipe extends CustomRecipe {
    public RepairBubbleBlowerRecipe(ResourceLocation idIn) {
        super(idIn);
    }


    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {

        ItemStack bubbleBlower = null;
        ItemStack soap = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
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
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack blower = null;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
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
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistry.BUBBLE_BLOWER_REPAIR_RECIPE.get();
    }


}
