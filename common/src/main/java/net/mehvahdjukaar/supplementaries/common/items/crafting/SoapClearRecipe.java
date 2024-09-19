package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class SoapClearRecipe extends CustomRecipe {
    public SoapClearRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingContainer, Level level) {
        int i = 0;
        int j = 0;

        for (int k = 0; k < craftingContainer.size(); ++k) {
            ItemStack itemstack = craftingContainer.getItem(k);
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                boolean isColored = (BlocksColorAPI.getColor(item) != null &&
                        !CommonConfigs.Functional.SOAP_DYE_CLEAN_BLACKLIST.get().contains(BlocksColorAPI.getKey(item)));
                if (isColored || item instanceof DyeableLeatherItem || hasTrim(item)) {
                    ++i;
                } else {
                    if (!itemstack.is(ModRegistry.SOAP.get())) {
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

    //TODO: add this and JEI view of it
    private boolean hasTrim(Item item) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack toRecolor = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (BlocksColorAPI.getColor(item) != null ||
                        item instanceof DyeableLeatherItem) {
                    toRecolor = stack.copyWithCount(1);
                }
            }
        }

        ItemStack result;
        Item i = toRecolor.getItem();
        if (i instanceof DyeableLeatherItem leatherItem) {
            result = toRecolor.copy();
            leatherItem.clearColor(result);
            return result;
        } else {
            Item recolored = BlocksColorAPI.changeColor(i, null);
            if (recolored != null) {
                result = recolored.getDefaultInstance();
            } else {
                result = toRecolor.copy();
            }
        }

        var tag = toRecolor.getTag();
        if (tag != null) {
            result.setTag(tag.copy());
        }
        result.setCount(1);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SOAP_CLEARING.get();
    }
}

