package net.mehvahdjukaar.supplementaries.items.crafting;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BlackboardDuplicateRecipe extends SpecialRecipe {
    public BlackboardDuplicateRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    private boolean isDrawnBlackboard(ItemStack stack){
        CompoundNBT compoundnbt = stack.getTagElement("BlockEntityTag");
        return compoundnbt != null && compoundnbt.contains("pixels_0");

    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;

        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            Item item = stack.getItem();
            if (item == Registry.BLACKBOARD_ITEM.get()) {

                if (isDrawnBlackboard(stack)) {
                    if (itemstack != null) {
                        return false;
                    }

                    itemstack = stack;
                } else {
                    if (itemstack1 != null) {
                        return false;
                    }

                    itemstack1 = stack;
                }
            }
        }

        return itemstack != null && itemstack1 != null;
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        for(int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if(isDrawnBlackboard(stack)){
                ItemStack s = stack.copy();
                s.setCount(1);
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = inv.getItem(i);
            if (!itemstack.isEmpty()) {
                if (itemstack.hasContainerItem()) {
                    nonnulllist.set(i, itemstack.getContainerItem());
                } else if (itemstack.hasTag() && isDrawnBlackboard(itemstack)) {
                    ItemStack itemstack1 = itemstack.copy();
                    itemstack1.setCount(1);
                    nonnulllist.set(i, itemstack1);
                }
            }
        }
        return nonnulllist;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Registry.BLACKBOARD_DUPLICATE_RECIPE.get();
    }


}
