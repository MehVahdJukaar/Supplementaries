package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.supplementaries.common.items.PresentItem;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class PresentDyeRecipe extends CustomRecipe {

    public PresentDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        int i = 0;
        int j = 0;

        for (int k = 0; k < inv.size(); ++k) {
            ItemStack itemstack = inv.getItem(k);
            if (!itemstack.isEmpty()) {
                if (itemstack.getItem() instanceof PresentItem) {
                    ++i;
                } else {
                    if (!itemstack.is(ModTags.DYES)) {
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
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack itemstack = ItemStack.EMPTY;
        DyeColor dyecolor = DyeColor.WHITE;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
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
        return itemstack.transmuteCopy(BlocksColorAPI.changeColor(itemstack.getItem(), dyecolor),1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height > 2;
    }

    @Override
    public String getGroup() {
        return "present_dye";
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PRESENT_DYE.get();
    }
}

