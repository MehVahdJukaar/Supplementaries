package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TrappedPresentRecipe extends CustomRecipe {

    public TrappedPresentRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingContainer, Level level) {
        int i = 0;
        int j = 0;
        for (int k = 0; k < craftingContainer.size(); ++k) {
            ItemStack itemstack = craftingContainer.getItem(k);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof PresentItem) {
                    var container = itemstack.get(DataComponents.CONTAINER);
                    if (container == null) {
                        return false;
                    }
                    ++i;
                } else {
                    if (!itemstack.is(Items.TRIPWIRE_HOOK)) {
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

    // we could have made this generic with codec but it's not worth it
    @Override
    public ItemStack assemble(CraftingInput craftingContainer, HolderLookup.Provider registryAccess) {
        ItemStack itemstack = ItemStack.EMPTY;
        DyeColor dyecolor = DyeColor.WHITE;
        for (int i = 0; i < craftingContainer.size(); ++i) {
            ItemStack stack = craftingContainer.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (item instanceof PresentItem pi) {
                    itemstack = stack;
                    dyecolor = pi.getColor();
                }
            }
        }
        ItemStack result = itemstack.transmuteCopy(ModRegistry.TRAPPED_PRESENTS.get(dyecolor).get(), 1);
        result.remove(ModComponents.ADDRESS.get());

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TRAPPED_PRESENT.get();
    }
}

