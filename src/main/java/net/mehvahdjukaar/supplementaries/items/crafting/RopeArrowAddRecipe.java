package net.mehvahdjukaar.supplementaries.items.crafting;

import net.mehvahdjukaar.supplementaries.common.ModTags;
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

        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Registry.ROPE_ARROW_ITEM.get() && stack.getDamageValue()!=0) {
                if (arrow != null) {
                    return false;
                }
                arrow = stack;
                missingRopes = arrow.getDamageValue();
            }
        }
        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if(ModTags.isTagged(ModTags.ROPES,stack.getItem())) {
                if (missingRopes <= 0) return false;
                rope = stack;
                missingRopes--;
            }
        }
        return arrow != null && rope != null;
    }



    @Override
    public ItemStack assemble(CraftingInventory inv) {
        int ropes = 0;
        ItemStack arrow = null;
        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if(ModTags.isTagged(ModTags.ROPES,stack.getItem())){
                ropes++;
            }
            if(stack.getItem() == Registry.ROPE_ARROW_ITEM.get()) {
                arrow = stack;
            }
        }
        ItemStack returnArrow = arrow.copy();
        returnArrow.setDamageValue(arrow.getDamageValue() - ropes);
        return returnArrow;

    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Registry.ROPE_ARROW_ADD_RECIPE.get();
    }


}
