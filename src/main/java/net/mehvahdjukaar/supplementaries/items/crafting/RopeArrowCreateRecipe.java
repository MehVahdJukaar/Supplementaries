package net.mehvahdjukaar.supplementaries.items.crafting;

import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RopeArrowCreateRecipe extends SpecialRecipe {
    public RopeArrowCreateRecipe(ResourceLocation idIn) {
        super(idIn);
    }



    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;

        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() == Items.ARROW) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            }
            if(ModTags.isTagged(ModTags.ROPES,stack.getItem())) {

                itemstack1 = stack;

            }
        }
        return itemstack != null && itemstack1 != null;
    }



    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        int ropes = 0;
        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            if(ModTags.isTagged(ModTags.ROPES,inv.getStackInSlot(i).getItem())){
                ropes++;
            }
        }
        ItemStack stack = new ItemStack(Registry.ROPE_ARROW_ITEM.get());
        stack.setDamage(stack.getMaxDamage()-ropes);
        return stack;

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
        return Registry.ROPE_ARROW_CREATE_RECIPE.get();
    }


}
