package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.block.IColored;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public class SoapClearRecipe extends CustomRecipe {
    public SoapClearRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    public boolean matches(CraftingContainer craftingContainer, Level level) {
        int i = 0;
        int j = 0;

        for (int k = 0; k < craftingContainer.getContainerSize(); ++k) {
            ItemStack itemstack = craftingContainer.getItem(k);
            if (!itemstack.isEmpty()) {
                Item item = itemstack.getItem();
                if (Block.byItem(item) instanceof ShulkerBoxBlock ||
                        IColored.getOptional(item).isPresent() || item instanceof DyeableLeatherItem) {
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

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemstack = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack stack = craftingContainer.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                if (IColored.getOptional(item).isPresent() || Block.byItem(item) instanceof ShulkerBoxBlock ||
                        item instanceof DyeableLeatherItem) {
                    itemstack = stack;
                }
            }
        }
        ItemStack result;
        Item i = itemstack.getItem();
        if (i instanceof DyeableLeatherItem leatherItem) {
            result = itemstack.copy();
            leatherItem.clearColor(result);
            return result;
        }
        var op = IColored.getOptional(i);
        if (op.isPresent()) {
            var colored = op.get();
            var r = colored.changeItemColor(colored.supportsBlankColor() ? null : DyeColor.WHITE);
            if (r != null) {
                result = r.getDefaultInstance();
            } else {
                result = itemstack.copy();
            }
        } else {
            result = Items.SHULKER_BOX.getDefaultInstance();
        }

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
        return ModRecipes.SOAP_CLEARING_RECIPE.get();
    }
}

