package net.mehvahdjukaar.supplementaries.items.crafting;

import net.mehvahdjukaar.supplementaries.items.PresentItem;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class PresentDyeRecipe extends SpecialRecipe {
    public PresentDyeRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    public boolean matches(CraftingInventory craftingContainer, World level) {
        int i = 0;
        int j = 0;

        for (int k = 0; k < craftingContainer.getContainerSize(); ++k) {
            ItemStack itemstack = craftingContainer.getItem(k);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof PresentItem) {
                    ++i;
                } else {
                    if (!Tags.Items.DYES.contains(itemstack.getItem())) {
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
    public ItemStack assemble(CraftingInventory craftingContainer) {
        ItemStack itemstack = ItemStack.EMPTY;
        DyeColor dyecolor = DyeColor.WHITE;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack stack = craftingContainer.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (item instanceof PresentItem) {
                    itemstack = stack;
                } else {
                    DyeColor tmp = DyeColor.getColor(stack);
                    if (tmp != null) dyecolor = tmp;
                }
            }
        }
        ItemStack result = new ItemStack(ModRegistry.PRESENTS_ITEMS.get(dyecolor).get());

        if (itemstack.hasTag()) {
            result.setTag(itemstack.getTag().copy());
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ModRegistry.PRESENT_DYE_RECIPE.get();
    }
}

