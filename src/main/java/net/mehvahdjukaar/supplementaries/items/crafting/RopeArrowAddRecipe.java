package net.mehvahdjukaar.supplementaries.items.crafting;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RopeArrowAddRecipe extends SpecialRecipe {
    public RopeArrowAddRecipe(ResourceLocation idIn) {
        super(idIn);
    }



    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {

        ItemStack arrow = null;
        ItemStack rope = null;
        int missingRopes = 0;

        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() == Registry.ROPE_ARROW_ITEM.get() && stack.getDamage()!=0) {
                if (arrow != null) {
                    return false;
                }
                arrow = stack;
                missingRopes = arrow.getDamage();
            }
        }
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if(stack.getItem() == Registry.ROPE_ITEM.get()) {
                if (missingRopes <= 0) return false;
                rope = stack;
                missingRopes--;
            }
        }
        return arrow != null && rope != null;
    }



    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        int ropes = 0;
        ItemStack arrow = null;
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if(stack.getItem() == Registry.ROPE_ITEM.get()){
                ropes++;
            }
            if(stack.getItem() == Registry.ROPE_ARROW_ITEM.get()) {
                arrow = stack;
            }
        }
        ItemStack returnArrow = arrow.copy();
        returnArrow.setDamage(arrow.getDamage() - ropes);
        return returnArrow;

    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Registry.ROPE_ARROW_ADD_RECIPE.get();
    }


}
