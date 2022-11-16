package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RopeArrowAddRecipe extends CustomRecipe {
    public RopeArrowAddRecipe(ResourceLocation idIn) {
        super(idIn);
    }



    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {

        ItemStack arrow = null;
        ItemStack rope = null;
        int missingRopes = 0;

        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == ModRegistry.ROPE_ARROW_ITEM.get() && stack.getDamageValue()!=0) {
                if (arrow != null) {
                    return false;
                }
                arrow = stack;
                missingRopes += arrow.getDamageValue();
            }
            else if(stack.is(ModTags.ROPES)) {
                rope = stack;
                missingRopes--;
            }
            else if(!stack.isEmpty())return false;
        }
        return arrow != null && rope != null && missingRopes>=0;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        int ropes = 0;
        ItemStack arrow = null;
        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if(stack.is(ModTags.ROPES)){
                ropes++;
            }
            if(stack.getItem() == ModRegistry.ROPE_ARROW_ITEM.get()) {
                arrow = stack;
            }
        }
        ItemStack returnArrow = arrow.copy();
        returnArrow.setDamageValue(arrow.getDamageValue() - ropes);
        return returnArrow;

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
        return ModRecipes.ROPE_ARROW_ADD.get();
    }


}
