package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.items.RopeArrowItem;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RopeArrowCreateRecipe extends CustomRecipe {
    public RopeArrowCreateRecipe(ResourceLocation idIn, CraftingBookCategory category) {
        super(idIn, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {

        ItemStack itemstack = null;
        int ropes = 0;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.ARROW) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            } else if (RopeArrowItem.isValidRope(stack)) {
                ropes++;
            } else if (!stack.isEmpty()) return false;
        }
        return itemstack != null && ropes > 0 && ropes < RopeArrowItem.getRopeCapacity();
    }


    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        int ropes = 0;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (RopeArrowItem.isValidRope(inv.getItem(i))) {
                ropes++;
            }
        }
        ItemStack stack = new ItemStack(ModRegistry.ROPE_ARROW_ITEM.get());
        stack.setDamageValue(RopeArrowItem.getRopeCapacity());
        RopeArrowItem.addRopes(stack, ropes);
        return stack;

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
        return ModRecipes.ROPE_ARROW_CREATE.get();
    }

}
