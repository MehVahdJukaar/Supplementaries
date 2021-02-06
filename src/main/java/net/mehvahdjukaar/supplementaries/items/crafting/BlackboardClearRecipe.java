package net.mehvahdjukaar.supplementaries.items.crafting;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BlackboardClearRecipe extends SpecialRecipe {
    public BlackboardClearRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    private boolean isDrawnBlackboard(ItemStack stack){
        if (stack.getItem() == Registry.BLACKBOARD_ITEM.get()) {
            CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
            return compoundnbt != null && compoundnbt.contains("pixels_0");
        }
        return false;
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;

        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (isDrawnBlackboard(stack)) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            }
            if(stack.getItem() == Items.WATER_BUCKET) {
                if (itemstack1 != null) {
                    return false;
                }
                itemstack1 = stack;
            }
        }
        return itemstack != null && itemstack1 != null;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        return new ItemStack(Registry.BLACKBOARD_ITEM.get());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = inv.getStackInSlot(i);
            if (!itemstack.isEmpty()) {
                if (!(itemstack.hasTag() && isDrawnBlackboard(itemstack))) {
                    ItemStack itemstack1 = itemstack.copy();
                    itemstack1.setCount(1);
                    nonnulllist.set(i, itemstack1);
                }
            }
        }

        return nonnulllist;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Registry.BLACKBOARD_CLEAR_RECIPE.get();
    }


}
