package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.block.util.IColored;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
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
                if (Block.byItem(itemstack.getItem()) instanceof ShulkerBoxBlock || itemstack.getItem() instanceof IColored) {
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
                if (item instanceof IColored || Block.byItem(item) instanceof ShulkerBoxBlock) {
                    itemstack = stack;
                }
            }
        }
        ItemStack result;
        Item i = itemstack.getItem();
        if (i instanceof IColored colored) {
            var map = colored.getItemColorMap();
            if (map != null) {
                result = map.get(colored.supportsBlankColor() ? null : DyeColor.WHITE).get().getDefaultInstance();
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
        return ModRegistry.SOAP_CLEARING_RECIPE.get();
    }
}

