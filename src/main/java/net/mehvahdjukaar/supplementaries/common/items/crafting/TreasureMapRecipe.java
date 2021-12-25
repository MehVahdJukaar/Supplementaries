package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.world.data.map.WeatheredMap;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TreasureMapRecipe extends CustomRecipe {
    public TreasureMapRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    private Level lastWorld = null;

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof MapItem) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            }
            if (stack.getItem() == ModRegistry.ANTIQUE_INK.get()) {

                if (itemstack1 != null) {
                    return false;
                }
                itemstack1 = stack;

            }
        }
        boolean match = itemstack != null && itemstack1 != null;
        if (match) {
            lastWorld = worldIn;
        }
        return match;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof MapItem) {
                ItemStack s = stack.copy();
                s.setCount(1);
                if (lastWorld instanceof ServerLevel level) {
                    WeatheredMap.setAntique(level, s);

                }
                return s;
            }
        }
        return ItemStack.EMPTY;
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
        return ModRegistry.TREASURE_MAP_RECIPE.get();
    }


}
