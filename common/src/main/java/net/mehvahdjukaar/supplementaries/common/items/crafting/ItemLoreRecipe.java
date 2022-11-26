package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ItemLoreRecipe extends CustomRecipe {
    public ItemLoreRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {

        ItemStack nameTag = null;
        ItemStack item = null;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.NAME_TAG && stack.hasCustomHoverName()) {
                if (nameTag != null) {
                    return false;
                }
                nameTag = stack;
            } else if (!stack.isEmpty()) {
                if (item != null) {
                    return false;
                }
                item = stack;
            }
        }
        return nameTag != null && item != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        ItemStack itemstack = ItemStack.EMPTY;
        ItemStack nameTag = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            var s = craftingContainer.getItem(i);
            if (s.getItem() == Items.NAME_TAG) {
                nameTag = s;
            } else if (!s.isEmpty()) {
                itemstack = s;
            }
            if (!nameTag.isEmpty() && !itemstack.isEmpty()) break;
        }

        Component lore = nameTag.getHoverName();
        ItemStack result = itemstack.copy();
        result.setCount(1);
        addLore(lore, result);
        return result;
    }

    public static void addLore(Component lore, ItemStack result) {
        CompoundTag tag = result.getOrCreateTag();
        if (tag.contains("display")) {
            tag = tag.getCompound("display");
        } else {
            var t = new CompoundTag();
            tag.put("display", t);
            tag = t;
        }

        ListTag list;
        if (tag.getTagType("Lore") == 9) {
            list = tag.getList("Lore", 8);
        } else list = new ListTag();

        list.add(StringTag.valueOf(Component.Serializer.toJson(lore)));

        tag.put("Lore", list);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack itemstack = inv.getItem(i);
            if (itemstack.is(Items.NAME_TAG)) {
                ItemStack copy = itemstack.copy();
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

