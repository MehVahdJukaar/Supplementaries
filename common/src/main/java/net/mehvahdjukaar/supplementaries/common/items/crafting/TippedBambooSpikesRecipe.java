package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TippedBambooSpikesRecipe extends CustomRecipe {
    public TippedBambooSpikesRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    private boolean isEmptySpike(ItemStack stack) {
        if (stack.getItem() == ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get()) {
            CompoundTag tag = stack.getTag();
            return tag != null && tag.getInt("Damage") != 0;
        } else return stack.getItem() == ModRegistry.BAMBOO_SPIKES_ITEM.get();
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {

        ItemStack itemstack = null;
        ItemStack stack1 = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (isEmptySpike(stack)) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            } else if (stack.getItem() == Items.LINGERING_POTION) {
                Potion potion = PotionUtils.getPotion(stack);
                var effects = potion.getEffects();
                if (effects.isEmpty()) return false;
                if (!BambooSpikesTippedItem.isPotionValid(potion)) return false;

                if (stack1 != null) {
                    return false;
                }
                stack1 = stack;
            } else if (!stack.isEmpty()) return false;
        }
        return itemstack != null && stack1 != null;
    }


    @Override
    public ItemStack assemble(CraftingContainer inv) {
        Potion potion = Potions.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            Potion p = PotionUtils.getPotion(inv.getItem(i));
            if (p != Potions.EMPTY) {
                potion = p;
                break;
            }
        }
        return BambooSpikesTippedItem.makeSpikeItem(potion);

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
        return ModRecipes.BAMBOO_SPIKES_TIPPED.get();
    }


}
