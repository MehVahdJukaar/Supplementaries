package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.misc.AntiqueInkHelper;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.WeatheredMap;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class WeatheredMapRecipe extends CustomRecipe {
    public WeatheredMapRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    private Level lastWorld = null;

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
            } else if (isMap(stack)) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            } else if (stack.getItem() == ModRegistry.ANTIQUE_INK.get() ||
                    (false && //disabled
                    stack.getItem() == ModRegistry.SOAP.get())) {

                if (itemstack1 != null) {
                    return false;
                }
                itemstack1 = stack;

            } else return false;
        }
        boolean match = itemstack != null && itemstack1 != null;
        if (match) {
            lastWorld = worldIn;
        }
        return match;
    }

    private static boolean isMap(ItemStack stack) {
        return stack.getItem() == Items.FILLED_MAP;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        boolean antique = true;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (inv.getItem(i).getItem() == ModRegistry.SOAP.get()) {
                antique = false;
                break;
            }
        }
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof MapItem) {
                ItemStack s = stack.copy();
                s.setCount(1);
                if (lastWorld instanceof ServerLevel level) {
                    WeatheredMap.setAntique(level, s, antique);
                    AntiqueInkHelper.setAntiqueInk(s,true);
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
        return ModRecipes.ANTIQUE_MAP.get();
    }


}
