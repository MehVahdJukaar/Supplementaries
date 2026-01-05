package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.mehvahdjukaar.supplementaries.common.misc.map_markers.WeatheredMap;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;

public class WeatheredMapRecipe extends CustomRecipe {
    public WeatheredMapRecipe(ResourceLocation idIn, CraftingBookCategory category) {
        super(idIn, category);
    }

    private static WeakReference<ServerLevel> lastLevelHack = null;

    public static void onWorldUnload(){
        lastLevelHack = null;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {

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
        if (match && level instanceof ServerLevel serverLevel) {
            lastLevelHack = new WeakReference<>(serverLevel);
        }
        return match;
    }

    private static boolean isMap(ItemStack stack) {
        return stack.getItem() == Items.FILLED_MAP;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
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
                if (lastLevelHack != null) {
                    WeatheredMap.setAntique(lastLevelHack.get(), s, antique, false);
                    AntiqueInkItem.setAntiqueInk(s,true);
                }
                return s;
            }
        }
        return ItemStack.EMPTY;
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
