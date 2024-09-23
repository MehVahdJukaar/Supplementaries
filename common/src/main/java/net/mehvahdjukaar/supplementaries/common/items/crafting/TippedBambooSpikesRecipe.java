package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.common.items.BambooSpikesTippedItem;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TippedBambooSpikesRecipe extends CustomRecipe {
    public TippedBambooSpikesRecipe(CraftingBookCategory category) {
        super(category);
    }

    private boolean isEmptySpike(ItemStack stack) {
        if (stack.getItem() == ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get()) {
            Integer charges = stack.get(ModComponents.CHARGES.get());
            return charges == null || charges == 0;
        } else return stack.is(ModRegistry.BAMBOO_SPIKES.get().asItem());
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        ItemStack itemstack = null;
        ItemStack stack1 = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (isEmptySpike(stack)) {
                if (itemstack != null) {
                    return false;
                }
                itemstack = stack;
            } else if (stack.getItem() == Items.LINGERING_POTION) {
                PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
                if (potion == null) return false;
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
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        PotionContents potion = PotionContents.EMPTY;
        for (int i = 0; i < inv.size(); ++i) {
            PotionContents p = inv.getItem(i).getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (p != PotionContents.EMPTY) {
                potion = p;
                break;
            }
        }
        ItemStack returnStack = ModRegistry.BAMBOO_SPIKES_TIPPED_ITEM.get().getDefaultInstance();
        returnStack.set(DataComponents.POTION_CONTENTS, potion);
        return returnStack;

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
