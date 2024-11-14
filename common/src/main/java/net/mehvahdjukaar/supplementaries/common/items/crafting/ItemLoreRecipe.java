package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

public class ItemLoreRecipe extends CustomRecipe {
    public ItemLoreRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        ItemStack nameTag = null;
        ItemStack item = null;
        boolean isSoap = false;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.NAME_TAG && stack.has(DataComponents.CUSTOM_NAME)) {
                if (nameTag != null) {
                    return false;
                }
                nameTag = stack;
            } else if (stack.is(ModRegistry.SOAP.get())) {
                if (nameTag != null) {
                    return false;
                }
                isSoap = true;
                nameTag = stack;
            } else if (!stack.isEmpty()) {
                if (item != null) {
                    return false;
                }
                item = stack;
            }
        }
        return nameTag != null && item != null &&
                (!isSoap || !item.getOrDefault(DataComponents.LORE, ItemLore.EMPTY ).lines().isEmpty());
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack nameTag = ItemStack.EMPTY;
        ItemStack soap = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); ++i) {
            var s = inv.getItem(i);
            if (s.getItem() == Items.NAME_TAG) {
                nameTag = s;
            } else if (s.is(ModRegistry.SOAP.get())) {
                soap = s;
            } else if (!s.isEmpty()) {
                itemstack = s;
            }
        }
        ItemStack result = itemstack.copyWithCount(1);

        if (!soap.isEmpty()) {
            result.remove(DataComponents.LORE);
        } else {
            Component lore = nameTag.getHoverName();
            result.set(DataComponents.LORE, new ItemLore(List.of(lore)));
        }
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack itemstack = inv.getItem(i);
            if (itemstack.is(Items.NAME_TAG)) {
                ItemStack copy = itemstack.copy();
                copy.setCount(1);
                stacks.set(i, copy);
                return stacks;
            }
        }
        return stacks;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return x * y >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ITEM_LORE.get();
    }
}

