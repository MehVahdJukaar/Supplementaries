package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class PresentDyeRecipe extends CustomRecipe {
    public PresentDyeRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    public boolean matches(CraftingContainer craftingContainer, Level level) {
        int i = 0;
        int j = 0;

        for (int k = 0; k < craftingContainer.getContainerSize(); ++k) {
            ItemStack itemstack = craftingContainer.getItem(k);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof PresentItem) {
                    ++i;
                } else {
                    if (!ForgeHelper.isDye(itemstack)) {
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
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemstack = ItemStack.EMPTY;
        DyeColor dyecolor = DyeColor.WHITE;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack stack = craftingContainer.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (item instanceof PresentItem) {
                    itemstack = stack;
                } else {
                    DyeColor tmp = ForgeHelper.getColor(stack);
                    if (tmp != null) dyecolor = tmp;
                }
            }
        }

        //improve this is crap
        ItemStack result = new ItemStack(((PresentItem) itemstack.getItem()).changeItemColor(dyecolor).asItem());

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
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PRESENT_DYE.get();
    }
}

