package net.mehvahdjukaar.supplementaries.items.crafting;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TippedBambooSpikesRecipe extends SpecialRecipe {
    public TippedBambooSpikesRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    private boolean isEmptySpike(ItemStack stack){
        if (stack.getItem() == Registry.BAMBOO_SPIKES_TIPPED_ITEM.get()) {
            CompoundNBT compoundnbt = stack.getTag();
            return compoundnbt != null && compoundnbt.getInt("Damage")!=0;
        }
        else return stack.getItem() == Registry.BAMBOO_SPIKES_ITEM.get();
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {

        ItemStack itemstack = null;
        ItemStack itemstack1 = null;

        for(int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (isEmptySpike(stack)) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            }
            if(stack.getItem() == Items.LINGERING_POTION && PotionUtils.getEffectsFromStack(stack).stream()
                    .map(EffectInstance::getPotion).anyMatch(effect -> effect.equals(Effects.POISON))) {

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
        return new ItemStack(Registry.BAMBOO_SPIKES_TIPPED_ITEM.get());
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
        return Registry.BAMBOO_SPIKES_TIPPED_RECIPE.get();
    }


}
