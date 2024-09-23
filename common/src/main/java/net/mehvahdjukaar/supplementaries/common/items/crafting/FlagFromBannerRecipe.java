package net.mehvahdjukaar.supplementaries.common.items.crafting;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.Optional;

public class FlagFromBannerRecipe extends CustomRecipe {
    public FlagFromBannerRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level world) {
        DyeColor dyecolor = null;
        ItemStack withPatterns = null;
        ItemStack empty = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack itemStack = inv.getItem(i);
            Item item = itemStack.getItem();
            if (item instanceof FlagItem flagItem) {
                if (dyecolor == null) {
                    dyecolor = flagItem.getColor();
                } else if (dyecolor != flagItem.getColor()) {
                    return false;
                }

                BannerPatternLayers patterns = itemStack.get(DataComponents.BANNER_PATTERNS);
                int j = patterns.layers().size();
                if (j > getMaxBannerPatterns()) {
                    return false;
                }

                if (j > 0) {
                    if (withPatterns != null) {
                        return false;
                    }

                    withPatterns = itemStack;
                } else {
                    if (empty != null) {
                        return false;
                    }

                    empty = itemStack;
                }

            } else if (item instanceof BannerItem banneritem) {
                if (dyecolor == null) {
                    dyecolor = banneritem.getColor();
                } else if (dyecolor != banneritem.getColor()) {
                    return false;
                }

                BannerPatternLayers patterns = itemStack.get(DataComponents.BANNER_PATTERNS);
                int j = patterns.layers().size();
                if (j > getMaxBannerPatterns()) {
                    return false;
                }
                //exclude banner to banner
                if (j > 0 && !(empty != null && empty.getItem() instanceof BannerItem)) {
                    if (withPatterns != null) {
                        return false;
                    }
                    withPatterns = itemStack;
                } else if (!(withPatterns != null && withPatterns.getItem() instanceof BannerItem)) {
                    if (empty != null) {
                        return false;
                    }

                    empty = itemStack;
                }
            } else if (!itemStack.isEmpty()) return false;
        }

        return withPatterns != null && empty != null;
    }

    private static int getMaxBannerPatterns() {
        return CompatHandler.QUARK ? QuarkCompat.getBannerPatternLimit(6) : 6;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider access) {
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack withPatterns = inv.getItem(i);
            if (!withPatterns.isEmpty()) {
                BannerPatternLayers patterns = withPatterns.get(DataComponents.BANNER_PATTERNS);
                //find item with patterns
                for (int k = 0; k < inv.size(); ++k) {
                    if (i != k) {
                        ItemStack empty = inv.getItem(k);
                        //find other which must be empty. exclude banner to banner
                        Item it = empty.getItem();
                        if (it instanceof FlagItem || it instanceof BannerItem) {
                            ItemStack result = empty.copy();
                            result.setCount(1);
                            result.set(DataComponents.BANNER_PATTERNS, patterns);
                            return result;
                        }
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

        for (int i = 0; i < stacks.size(); ++i) {
            ItemStack itemstack = inv.getItem(i);
            if (!itemstack.isEmpty()) {
                Optional<ItemStack> container = ForgeHelper.getCraftingRemainingItem(itemstack);
                if (container.isPresent()) {
                    stacks.set(i, container.get());
                } else if (itemstack.has(DataComponents.BANNER_PATTERNS)) {
                    ItemStack copy = itemstack.copy();
                    copy.setCount(1);
                    stacks.set(i, copy);
                }
            }
        }

        return stacks;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FLAG_FROM_BANNER.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
}