package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class SafeRecipe extends CustomRecipe {
    public SafeRecipe( CraftingBookCategory category) {
        super( category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {

        ItemStack shulker = null;
        ItemStack netherite = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ModTags.SHULKER_BOXES)) {
                if (shulker != null) {
                    return false;
                }
                shulker = stack;
            } else if (stack.is(Items.NETHERITE_INGOT)) {
                if (netherite != null) {
                    return false;
                }
                netherite = stack;
            } else if (!stack.isEmpty()) {
                return false;
            }
        }
        return shulker != null && netherite != null;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack shulker = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ModTags.SHULKER_BOXES)) {
                shulker = stack;
                break;
            }
        }
        return shulker.transmuteCopy(ModRegistry.SACK_ITEM.get(), 1);
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SAFE.get();
    }
}

